package pl.margoj.server.implementation.database.caches.inventory

import pl.margoj.server.implementation.database.DatabaseManager
import pl.margoj.server.implementation.database.TableNames
import pl.margoj.server.implementation.inventory.map.MapInventoryImpl
import pl.margoj.server.implementation.item.ItemStackImpl
import java.sql.PreparedStatement
import java.sql.ResultSet

class MapInventoryCache(databaseManager: DatabaseManager) : AbstractInventoryCache<String, MapInventoryImpl>
(
        databaseManager,
        TableNames.MAP_INVENTORIES,
        searchBy = "owner",
        columnNames = *arrayOf("index", "owner", "item")
)
{
    override fun idFromString(string: String): String?
    {
        return string
    }

    override fun setIdInSQL(statement: PreparedStatement, index: Int, id: String)
    {
        statement.setString(index, id)
    }

    override fun getIdOf(data: MapInventoryImpl): String
    {
        return data.map.id
    }

    override fun canWipe(data: MapInventoryImpl): Boolean
    {
        return false
    }

    override fun getIdFromResultSet(resultSet: ResultSet): String
    {
        return resultSet.getString("owner")
    }

    override fun newInventory(id: String): MapInventoryImpl
    {
        val inventory = MapInventoryImpl()
        inventory.mapId = id
        return inventory
    }

    override fun populateResultSet(statement: PreparedStatement, i: () -> Int, d: ItemStackImpl)
    {
        statement.setInt(i(), d.ownerIndex!!)
        statement.setString(i(), (d.owner as MapInventoryImpl).map.id)
        statement.setLong(i(), d.id)
    }
}