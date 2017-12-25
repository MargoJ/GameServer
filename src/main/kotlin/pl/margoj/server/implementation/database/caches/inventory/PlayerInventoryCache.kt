package pl.margoj.server.implementation.database.caches.inventory

import pl.margoj.utils.commons.numbers.Parse
import pl.margoj.server.implementation.database.DatabaseManager
import pl.margoj.server.implementation.database.TableNames
import pl.margoj.server.implementation.inventory.player.PlayerInventoryImpl
import pl.margoj.server.implementation.item.ItemStackImpl
import java.sql.PreparedStatement
import java.sql.ResultSet

class PlayerInventoryCache(databaseManager: DatabaseManager) : AbstractInventoryCache<Long, PlayerInventoryImpl>
(
        databaseManager,
        TableNames.PLAYER_INVENTORIES,
        searchBy = "owner",
        columnNames = *arrayOf("index", "owner", "item")
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

    override fun getIdOf(data: PlayerInventoryImpl): Long
    {
        if (data.id == null)
        {
            data.id = data.player.id
        }

        return data.id!!.toLong()
    }

    override fun canWipe(data: PlayerInventoryImpl): Boolean
    {
        return !data.player.online
    }

    override fun getIdFromResultSet(resultSet: ResultSet): Long
    {
        return resultSet.getLong("owner")
    }

    override fun newInventory(id: Long): PlayerInventoryImpl
    {
        return PlayerInventoryImpl()
    }

    override fun populateResultSet(statement: PreparedStatement, i: () -> Int, d: ItemStackImpl)
    {
        statement.setInt(i(), d.ownerIndex!!)
        statement.setLong(i(), (d.owner as PlayerInventoryImpl).player.id.toLong())
        statement.setLong(i(), d.id)
    }
}