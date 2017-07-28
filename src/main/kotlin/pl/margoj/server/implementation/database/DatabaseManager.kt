package pl.margoj.server.implementation.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.database.caches.ItemDataCache
import pl.margoj.server.implementation.database.caches.PlayerDataCache
import pl.margoj.server.implementation.database.caches.inventory.MapInventoryCache
import pl.margoj.server.implementation.database.caches.inventory.PlayerInventoryCache
import java.sql.Connection
import java.util.concurrent.atomic.AtomicInteger
import javax.sql.DataSource

class DatabaseManager(val server: ServerImpl)
{
    private var threadConnection = ThreadLocal.withInitial { ThreadConnection() }
    private var skipCheck = ThreadLocal.withInitial { false }
    private var dataSource: HikariDataSource? = null

    lateinit var playerDataCache: PlayerDataCache
        private set

    lateinit var itemDataCache: ItemDataCache
        private set

    lateinit var playerInventoryCache: PlayerInventoryCache
        private set

    lateinit var mapInventoryDataCache: MapInventoryCache
        private set

    fun start()
    {
        val mysqlConfig = server.config.mySQLConfig
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:mysql://${mysqlConfig.ip}/${mysqlConfig.database}?useSSL=false"
        config.username = mysqlConfig.username
        config.password = mysqlConfig.password
        config.driverClassName = "com.mysql.jdbc.Driver"
        config.maximumPoolSize = mysqlConfig.maxConnectionPoolSize

        this.dataSource = HikariDataSource(config)

        DatabaseInitializer(this).init()

        this.playerDataCache = PlayerDataCache(this)
        this.itemDataCache = ItemDataCache(this)
        this.playerInventoryCache = PlayerInventoryCache(this)
        this.mapInventoryDataCache = MapInventoryCache(this)
    }

    fun saveAll(): Int
    {
        var all = 0
        all += this.saveCache(this.playerDataCache)
        all += this.saveCache(this.itemDataCache)
        all += this.saveCache(this.playerInventoryCache)
        all += this.saveCache(this.mapInventoryDataCache)
        return all
    }

    private fun saveCache(cache: DatabaseObjectCache<*, *>): Int
    {
        return cache.saveToDatabase()
    }

    fun withConnection(action: (Connection) -> Unit)
    {
        if (!this.skipCheck.get() && this.server.ticker.isInMainThread)
        {
            throw IllegalStateException("prohibited in main thread")
        }

        this.withConnectionUnsafe(action)
    }

    internal fun withConnectionUnsafe(action: (Connection) -> Unit)
    {
        val threadConnection = this.threadConnection.get()
        val connection = threadConnection.request(this.dataSource!!)

        try
        {
            action(connection)
        }
        finally
        {
            threadConnection.release()
        }
    }

    internal fun withSkippedCheck(action: () -> Unit)
    {
        try
        {
            this.skipCheck.set(true)
            action()
        }
        finally
        {
            this.skipCheck.set(false)
        }
    }

    fun stop()
    {
        this.server.logger.info("Zapisuje dane do bazy danych")
        this.server.logger.info("Zapisano ${this.saveAll()} rekordów w bazie danych")
        this.dataSource?.close()
    }

    fun preloadData()
    {
        // map inventories
        val allTowns = this.server.towns.map { it.id }
        val inventories = this.mapInventoryDataCache.load(allTowns)

        for (inventory in inventories)
        {
            if (inventory != null)
            {
                val town = this.server.getTownById(inventory.mapId)
                if (town != null)
                {
                    town.inventory = inventory
                    inventory.map = town
                }
            }
        }
    }
}

private class ThreadConnection
{
    private var connection: Connection? = null
    private val usageCount = AtomicInteger()

    fun request(dataSource: DataSource): Connection
    {
        DatabaseObjectCache.logger.trace("ThreadConnection[${Thread.currentThread()}] - request")
        if (connection == null)
        {
            usageCount.set(0)
            connection = dataSource.getConnection()
            DatabaseObjectCache.logger.trace("ThreadConnection[${Thread.currentThread()}] - requested new connection")
        }

        val newUsageCount = usageCount.incrementAndGet()
        DatabaseObjectCache.logger.trace("ThreadConnection[${Thread.currentThread()}] - requested usageCount=$newUsageCount")

        return connection!!
    }

    fun release()
    {
        val newUsageCount = usageCount.decrementAndGet()

        DatabaseObjectCache.logger.trace("ThreadConnection[${Thread.currentThread()}] - releasing usageCount=$newUsageCount")

        if (newUsageCount == 0)
        {
            connection!!.close()
            connection = null
            DatabaseObjectCache.logger.trace("ThreadConnection[${Thread.currentThread()}] - closed")
        }
    }
}