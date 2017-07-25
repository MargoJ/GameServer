package pl.margoj.server.implementation.database.caches

import pl.margoj.server.implementation.database.DatabaseManager
import pl.margoj.server.implementation.database.DatabaseObjectCache
import pl.margoj.server.implementation.database.TableNames
import pl.margoj.server.implementation.inventory.player.PlayerInventoryImpl
import java.sql.Connection

class PlayerInventoryDataCache(databaseManager: DatabaseManager) : DatabaseObjectCache<PlayerInventoryImpl>
(
        databaseManager,
        TableNames.PLAYER_INTENTORIES,
        searchBy = "owner",
        multipleLoad = false,
        rawColumns = *arrayOf("index", "owner", "item")
)
{
    override fun getIdOf(data: PlayerInventoryImpl): Long
    {
        return data.id!!.toLong()
    }

    override fun loadFromDatabase(connection: Connection, id: LongArray): List<PlayerInventoryImpl?>
    {
        return this.tryLoad(connection, id) {
            val inventory = PlayerInventoryImpl()
            inventory.id = id[0].toInt()

            val itemIds = hashMapOf<Long, Int>()
            while (it.next())
            {
                itemIds.put(it.getLong("item"), it.getInt("index"))
            }

            val items = databaseManager.itemDataCache.load(itemIds.keys.toLongArray())
            for (item in items)
            {
                if (item != null)
                {
                    inventory[itemIds[item.id]!!] = item
                }
            }

            inventory
        }
    }

    override fun saveToDatabase(connection: Connection, data: Collection<PlayerInventoryImpl?>)
    {
        this.trySave(connection, data) { d, statement, i, last ->
            if (!last)
            {
                return@trySave
            }

            statement.addBatch("DELETE FROM `player_inventories` WHERE `owner`=${d.player.id}")

            if (d.id == null)
            {
                d.id = d.player.id
            }

            for (item in d.allItems)
            {
                if (item != null)
                {
                    var n = 1
                    for (ignored in 0..1)
                    {
                        statement.setInt(n++, item.ownerIndex!!)
                        statement.setLong(n++, d.player.id.toLong())
                        statement.setLong(n++, item.id)
                    }

                    statement.addBatch()
                }
            }
        }
    }

    override fun canWipe(data: PlayerInventoryImpl): Boolean
    {
        return !data.player.online
    }
}