package pl.margoj.server.implementation.database.caches.inventory

import pl.margoj.server.implementation.database.DatabaseManager
import pl.margoj.server.implementation.database.DatabaseObjectCache
import pl.margoj.server.implementation.inventory.AbstractInventoryImpl
import pl.margoj.server.implementation.item.ItemStackImpl
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.LinkedList

abstract class AbstractInventoryCache<I, T : AbstractInventoryImpl>(
        databaseManager: DatabaseManager,
        tableName: String,
        searchBy: String,
        vararg columnNames: String
) : DatabaseObjectCache<I, T>(
        databaseManager,
        tableName,
        searchBy = searchBy,
        simpleLoading = false,
        rawColumns = *columnNames
)
{

    protected abstract fun getIdFromResultSet(resultSet: ResultSet): I

    protected abstract fun newInventory(id: I): T

    override fun loadFromDatabase(connection: Connection, ids: Collection<I>): List<T?>
    {
        val itemsToLoad = LinkedList<Long>()
        val inventoriesPrepared = HashMap<I, MutableMap<Int, Long>>(ids.size)

        this.tryLoad(connection, ids) {
            while (it.next())
            {
                val item = it.getLong("item")
                itemsToLoad.add(item)
                inventoriesPrepared.computeIfAbsent(this.getIdFromResultSet(it), { LinkedHashMap<Int, Long>() }).put(it.getInt("index"), item)
            }

            null
        }

        val rawItems = this.databaseManager.itemDataCache.load(itemsToLoad)
        val items = hashMapOf<Long, ItemStackImpl>()
        rawItems.filterNotNull().forEach { items.put(it.id, it) }

        val list = ArrayList<T?>(ids.size)

        for (id in ids)
        {
            val inventory = this.newInventory(id)

            val prepared = inventoriesPrepared.get(id)
            if (prepared != null)
            {
                for ((index, item) in prepared)
                {
                    inventory[index] = items[item]
                }
            }

            list.add(inventory)
            this.cache.put(id, inventory)
        }

        return list
    }


    override fun saveToDatabase(connection: Connection, data: Collection<T?>)
    {
        val cleanupStatement = connection.prepareStatement("DELETE FROM `${this.tableName}` WHERE `owner`=?")

        this.trySave(connection, data, { d, statement, i, last ->
            if (!last)
            {
                return@trySave
            }

            this.setIdInSQL(cleanupStatement, 1, this.getIdOf(d))
            cleanupStatement.addBatch()

            for (item in d.allItems)
            {
                if (item != null)
                {
                    var n = 1
                    val indexer: () -> Int = { n++ }

                    for (ignored in 0..1)
                    {
                        this.populateResultSet(statement, indexer, item)
                    }

                    statement.addBatch()
                }
            }
        }, {
            cleanupStatement.executeBatch()
        })
    }

    abstract fun populateResultSet(statement: PreparedStatement, i: () -> Int, d: ItemStackImpl)
}