package pl.margoj.server.implementation.database.caches

import pl.margoj.server.implementation.database.DatabaseManager
import pl.margoj.server.implementation.database.DatabaseObjectCache
import pl.margoj.server.implementation.database.TableNames
import pl.margoj.server.implementation.inventory.player.PlayerInventoryImpl
import pl.margoj.server.implementation.item.ItemImpl
import pl.margoj.server.implementation.item.ItemStackImpl
import java.sql.Blob
import java.sql.Connection

class ItemDataCache(databaseManager: DatabaseManager) : DatabaseObjectCache<ItemStackImpl>
(
        databaseManager,
        TableNames.ITEMS,
        rawColumns = *arrayOf("id", "item_id", "properties")
)
{
    override fun getIdOf(data: ItemStackImpl): Long
    {
        return data.id
    }

    override fun loadFromDatabase(connection: Connection, id: LongArray): List<ItemStackImpl?>
    {
        return this.tryLoad(connection, id) {
            val itemManager = databaseManager.server.itemManager
            var item: ItemStackImpl? = null
            itemManager.loadNewItem {
                item = ItemStackImpl(databaseManager.server.itemManager, databaseManager.server.getItemById(it.getString("item_id")) as ItemImpl, it.getLong("id"))
            }
            item!!
        }
    }

    override fun saveToDatabase(connection: Connection, data: Collection<ItemStackImpl?>)
    {
        this.trySave(connection, data) { d, statement, i, last ->
            statement.setLong(i(), d.id)
            statement.setString(i(), d.item.id)
            statement.setBlob(i(), null as Blob?)
        }
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

        return true
    }
}