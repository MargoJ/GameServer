package pl.margoj.server.implementation.database

import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.Arrays
import java.util.Collections
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

abstract class DatabaseObjectCache<T>
(
        val databaseManager: DatabaseManager,
        val tableName: String,
        val searchBy: String = "id",
        val multipleLoad: Boolean = true,
        vararg val rawColumns: String
)
{
    private companion object
    {
        val logger = LogManager.getLogger("Cache")
    }

    init
    {
        logger.trace("Creating new cache ${this.javaClass}")
    }

    private val logPrefix = this.javaClass.name
    private val lock = ReentrantLock()
    private val cache = LinkedHashMap<Long, T?>()

    var lastSave: Long = 0
        private set

    val cached: Int get() = this.cache.size

    fun loadOne(id: Long): T?
    {
        logger.trace("$logPrefix.loadOne($id)")
        val ids = LongArray(1)
        ids[0] = id
        val out = this.load(ids)
        return if (out.isEmpty()) null else out.iterator().next()
    }

    fun load(ids: LongArray): List<T?>
    {
        logger.trace("$logPrefix.loadOne(${Arrays.toString(ids)})")

        val out = ArrayList<T?>(ids.size)

        if (!this.multipleLoad && ids.size != 1)
        {
            throw IllegalStateException("multiple load not supported")
        }

        this.lock.withLock {
            val fromDb = ArrayList<Long>()
            for (id in ids)
            {
                if (cache.containsKey(id))
                {
                    out.add(cache[id])
                    logger.info("$logPrefix $id loaded from cache")
                }
                else
                {
                    fromDb.add(id)
                }
            }

            if (!fromDb.isEmpty())
            {
                val idsFromDb = LongArray(fromDb.size, { fromDb[it] })

                this.databaseManager.withConnection {
                    logger.info("$logPrefix - loading from database (${Arrays.toString(idsFromDb)})")
                    this.loadFromDatabase(it, idsFromDb).mapTo(out) { it }
                }
            }
        }

        return out
    }

    fun saveOne(data: T)
    {
        logger.trace("$logPrefix.saveOne($data)")
        this.save(Collections.singleton(data))
    }

    fun save(data: Collection<T>)
    {
        logger.trace("$logPrefix.save($data)")

        this.lock.withLock {
            for (element in data)
            {
                logger.info("$logPrefix - putting to cache $element")
                this.cache.put(this.getIdOf(element), element)
            }
        }
    }

    fun saveToDatabase(): Int
    {
        logger.trace("$logPrefix.saveToDatabase()")
        val saved = this.cache.values.filter { it != null }.size
        logger.debug("saved - $saved")

        this.lock.withLock {
            this.databaseManager.withConnection {
                if (this.multipleLoad)
                {
                    this.saveToDatabase(it, this.cache.values)
                }

                val iterator = this.cache.values.iterator()

                while (iterator.hasNext())
                {
                    val data = iterator.next()

                    if (!this.multipleLoad && data != null)
                    {
                        this.saveToDatabase(it, Collections.singletonList(data))
                    }

                    if (data == null || this.canWipe(data))
                    {
                        logger.info("$logPrefix - wiped $data")
                        iterator.remove()
                    }
                }
            }

            this.lastSave = System.currentTimeMillis()
        }
        return saved
    }

    internal fun getOnlyFromCache(id: Long): T?
    {
        return this.cache[id]
    }

    internal fun remove(id: Long)
    {
        this.cache.remove(id)
    }

    abstract fun getIdOf(data: T): Long

    abstract fun canWipe(data: T): Boolean

    protected abstract fun loadFromDatabase(connection: Connection, id: LongArray): List<T?>

    protected abstract fun saveToDatabase(connection: Connection, data: Collection<T?>)

    protected fun trySave(connection: Connection, data: Collection<T?>, populator: (data: T, statement: PreparedStatement, indexer: () -> Int, Boolean) -> Unit)
    {
        val columns = rawColumns.map { "`$it`" }
        val queryBuilder = StringBuilder()
        queryBuilder.append("INSERT INTO `$tableName`(")
        queryBuilder.append(StringUtils.join(columns, ", "))
        queryBuilder.append(") VALUES (")
        queryBuilder.append(StringUtils.join(Array<String>(columns.size, { "?" }), ", "))
        queryBuilder.append(") ON DUPLICATE KEY UPDATE ")
        queryBuilder.append(StringUtils.join(Array<String>(columns.size, { "${columns[it]}=?" }), ", "))

        val updateQuery = connection.prepareStatement(queryBuilder.toString())

        var any = false

        for (element in data)
        {
            if (element == null)
            {
                continue
            }

            any = true

            var index = 1
            val indexer: () -> Int = { index++ }

            populator(element, updateQuery, indexer, false)
            populator(element, updateQuery, indexer, true)

            if (this.multipleLoad)
            {
                updateQuery.addBatch()
            }

            this.cache.put(this.getIdOf(element), element)
        }

        if (any)
        {
            updateQuery.executeBatch()
        }
    }

    protected fun tryLoad(connection: Connection, ids: LongArray, loader: (ResultSet) -> T): List<T?>
    {
        val array = StringUtils.join(ids.toTypedArray(), ", ")
        val resultSet = connection.createStatement().executeQuery("SELECT * FROM `$tableName` WHERE `$searchBy` IN ($array)")

        val out = ArrayList<T?>(ids.size)

        if (multipleLoad)
        {
            while (resultSet.next())
            {
                val element = loader(resultSet)
                this.cache.put(this.getIdOf(element), element)
                out.add(element)
            }
        }
        else
        {
            val element = loader(resultSet)
            this.cache.put(this.getIdOf(element), element)
            out.add(element)
        }

        return out
    }
}