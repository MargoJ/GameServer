package pl.margoj.server.implementation.database

import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.Collections
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

abstract class DatabaseObjectCache<I, T>
(
        val databaseManager: DatabaseManager,
        val tableName: String,
        val searchBy: String = "id",
        val simpleLoading: Boolean = true,
        vararg val rawColumns: String
)
{
    companion object
    {
        val logger = LogManager.getLogger("Cache")!!
    }

    init
    {
        logger.trace("Creating new cache ${this.javaClass}")
    }

    private val logPrefix = this.javaClass.name
    private val lock = ReentrantLock()
    protected val cache = LinkedHashMap<I, T?>()

    var lastSave: Long = 0
        private set

    val cached: Int get() = this.cache.size

    fun loadOne(id: I): T?
    {
        logger.trace("$logPrefix.loadOne($id)")
        val out = this.load(Collections.singletonList(id))
        return if (out.isEmpty()) null else out.iterator().next()
    }

    fun load(ids: Collection<I>): List<T?>
    {
        logger.trace("$logPrefix.loadOne($ids})")

        val out = ArrayList<T?>(ids.size)

        this.lock.withLock {
            val fromDb = ArrayList<I>(ids.size)
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
                this.databaseManager.withConnection {
                    logger.info("$logPrefix - loading from database ($fromDb)")
                    this.loadFromDatabase(it, fromDb).mapTo(out) { it }
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
                this.saveToDatabase(it, this.cache.values)

                val iterator = this.cache.values.iterator()

                while (iterator.hasNext())
                {
                    val data = iterator.next()

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

    internal fun getOnlyFromCache(id: I): T?
    {
        return this.cache[id]
    }

    internal fun remove(id: I)
    {
        this.cache.remove(id)
    }

    abstract fun getIdOf(data: T): I

    abstract fun canWipe(data: T): Boolean

    protected abstract fun loadFromDatabase(connection: Connection, ids: Collection<I>): List<T?>

    protected abstract fun saveToDatabase(connection: Connection, data: Collection<T?>)

    protected abstract fun setIdInSQL(statement: PreparedStatement, index: Int, id: I)

    protected abstract fun idFromString(string: String): I?

    protected fun trySave(connection: Connection, data: Collection<T?>, populator: (data: T, statement: PreparedStatement, indexer: () -> Int, Boolean) -> Unit, beforeBatch: () -> Unit = {})
    {
        val columns = rawColumns.map { "`$it`" }
        val queryBuilder = StringBuilder()
        queryBuilder.append("INSERT INTO `$tableName`(")
        queryBuilder.append(StringUtils.join(columns, ", "))
        queryBuilder.append(") VALUES (")
        queryBuilder.append(StringUtils.join(Array(columns.size, { "?" }), ", "))
        queryBuilder.append(") ON DUPLICATE KEY UPDATE ")
        queryBuilder.append(StringUtils.join(Array(columns.size, { "${columns[it]}=?" }), ", "))

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

            if (this.simpleLoading)
            {
                updateQuery.addBatch()
            }

            this.cache.put(this.getIdOf(element), element)
        }

        if (any)
        {
            beforeBatch()
            updateQuery.executeBatch()
        }
    }

    protected fun tryLoad(connection: Connection, ids: Collection<I>, loader: (ResultSet) -> T?): List<T?>
    {
        val queryBuilder = StringBuilder("SELECT * FROM `$tableName` WHERE `$searchBy` IN(")
        for ((index, _) in ids.withIndex())
        {
            queryBuilder.append("?")
            if (index != ids.size - 1)
            {
                queryBuilder.append(", ")
            }
        }
        queryBuilder.append(")")

        val statement = connection.prepareStatement(queryBuilder.toString())
        for((index, id) in ids.withIndex())
        {
            this.setIdInSQL(statement, index + 1, id)
        }

        val resultSet = statement.executeQuery()

        val out = ArrayList<T?>(ids.size)

        if (simpleLoading)
        {
            while (resultSet.next())
            {
                val element = loader(resultSet)!!
                this.cache.put(this.getIdOf(element), element)
                out.add(element)
            }

            return out
        }
        else
        {
            loader(resultSet)
            return emptyList()
        }
    }
}