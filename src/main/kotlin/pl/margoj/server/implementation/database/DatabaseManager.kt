package pl.margoj.server.implementation.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.database.caches.ItemDataCache
import pl.margoj.server.implementation.database.caches.PlayerDataCache
import pl.margoj.server.implementation.database.caches.PlayerInventoryDataCache
import java.sql.Connection

class DatabaseManager(val server: ServerImpl)
{
    private var dataSource: HikariDataSource? = null

    lateinit var playerDataCache: PlayerDataCache
        private set

    lateinit var itemDataCache: ItemDataCache
        private set

    lateinit var playerInventoryCache: PlayerInventoryDataCache
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
        this.playerInventoryCache = PlayerInventoryDataCache(this)
    }

    fun saveAll(): Int
    {
        var all = 0
        all += this.saveCache(this.playerDataCache)
        all += this.saveCache(this.itemDataCache)
        all += this.saveCache(this.playerInventoryCache)
        return all
    }

    private fun saveCache(cache: DatabaseObjectCache<*>): Int
    {
        return cache.saveToDatabase()
    }

    fun withConnection(action: (Connection) -> Unit)
    {
        if (this.server.ticker.isInMainThread)
        {
            throw IllegalStateException("prohibited in main thread")
        }

        this.withConnectionUnsafe(action)
    }

    internal fun withConnectionUnsafe(action: (Connection) -> Unit)
    {
        this.dataSource!!.getConnection().use {
            it.autoCommit = true
            action(it)
        }
    }

    fun stop()
    {
        this.server.logger.info("Zapisuje dane do bazy danych")
        this.server.logger.info("Zapisano ${this.saveAll()} rekordów w bazie danych")
        this.dataSource?.close()
    }
}