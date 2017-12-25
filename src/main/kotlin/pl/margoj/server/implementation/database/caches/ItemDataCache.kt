package pl.margoj.server.implementation.database.caches

import pl.margoj.utils.commons.numbers.Parse
import pl.margoj.server.implementation.database.DatabaseManager
import pl.margoj.server.implementation.database.DatabaseObjectCache
import pl.margoj.server.implementation.database.TableNames
import pl.margoj.server.implementation.inventory.map.MapInventoryImpl
import pl.margoj.server.implementation.inventory.player.PlayerInventoryImpl
import pl.margoj.server.implementation.item.ItemImpl
import pl.margoj.server.implementation.item.ItemStackImpl
import java.sql.Blob
import java.sql.Connection
import java.sql.PreparedStatement

class ItemDataCache(databaseManager: DatabaseManager) : DatabaseObjectCache<Long, ItemStackImpl>
(
        databaseManager,
        TableNames.ITEMS,
        rawColumns = *arrayOf("id", "item_id", "properties")
)
{
    override fun idFromString(string: String): Long?
    {
        return Parse.parseLong(string)
    }

    override fun setIdInSQL(statement: PreparedStatement, index: Int, id: Long)
    {
        statement.setLong(index, id)
    }

    override fun getIdOf(data: ItemStackImpl): Long
    {
        return data.id
    }

    override fun loadFromDatabase(connection: Connection, ids: Collection<Long>): List<ItemStackImpl?>
    {
        return this.tryLoad(connection, ids) {
            val itemManager = databaseManager.server.itemManager
            var item: ItemStackImpl? = null
            itemManager.loadNewItem {
                item = ItemStackImpl(databaseManager.server.itemManager, databaseManager.server.getItemById(it.getString("item_id")) as ItemImpl, it.getLong("id"))
            }
            val blob = it.getBlob("properties")
            if (blob != null)
            {
                item!!.deserializeAdditionalProperties(blob.getBytes(1, blob.length().toInt()))
            }
            item!!
        }
    }

    override fun saveToDatabase(connection: Connection, data: Collection<ItemStackImpl?>)
    {
        this.trySave(connection, data, { d, statement, i, _ ->
            statement.setLong(i(), d.id)
            statement.setString(i(), d.item.id)

            val propertiesBytes = d.serializeAdditionalProperties()
            if (propertiesBytes != null)
            {
                val blob = connection.createBlob()
                blob.setBytes(1, propertiesBytes)
                statement.setBlob(i(), blob)
            }
            else
            {
                statement.setBlob(i(), null as Blob?)
            }
        })
    }

    override fun canWipe(data: ItemStackImpl): Boolean
    {
        if (data.owner == null)
        {
            return true
        }

        if (data.owner is PlayerInventoryImpl)
        {
            return !(data.owner as PlayerInventoryImpl).player.online
        }
        if (data.owner is MapInventoryImpl)
        {
            return false
        }

        return true
    }
}