package pl.margoj.server.implementation.database.caches

import pl.margoj.server.api.map.Location
import pl.margoj.server.api.player.Profession
import pl.margoj.server.api.utils.Parse
import pl.margoj.server.implementation.database.DatabaseManager
import pl.margoj.server.implementation.database.DatabaseObjectCache
import pl.margoj.server.implementation.database.TableNames
import pl.margoj.server.implementation.player.PlayerDataImpl
import java.sql.Connection
import java.sql.PreparedStatement

class PlayerDataCache(databaseManager: DatabaseManager) : DatabaseObjectCache<Long, PlayerDataImpl>
(
        databaseManager,
        TableNames.PLAYERS,
        rawColumns = *arrayOf(
                "id", "characterName", "profession", "experience", "level", "map", "x", "y", "baseStrength", "baseAgility", "baseIntellect", "statPoints",
                "gold", "ttl", "dead_until"
        )
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

    override fun getIdOf(data: PlayerDataImpl): Long
    {
        return data.id
    }

    override fun loadFromDatabase(connection: Connection, ids: Collection<Long>): List<PlayerDataImpl?>
    {
        return this.tryLoad(connection, ids) {
            val data = PlayerDataImpl(it.getLong("id"), it.getString("characterName"))
            data.profession = Profession.getById(it.getString("profession")[0])!!
            data.xp = it.getLong("experience")
            data.level = it.getInt("level")
            val mapName = it.getString("map")
            if (mapName != null)
            {
                data.location = Location(
                        databaseManager.server.getTownById(mapName),
                        it.getInt("x"),
                        it.getInt("y")
                )
            }
            data.baseStrength = it.getInt("baseStrength")
            data.baseAgility = it.getInt("baseAgility")
            data.baseIntellect = it.getInt("baseIntellect")
            data.statPoints = it.getInt("statPoints")
            data.gold = it.getLong("gold")
            data.ttl = it.getInt("ttl")
            data.deadUntil = it.getDate("dead_until")
            data.inventory = databaseManager.playerInventoryCache.loadOne(data.id)
            data
        }
    }

    override fun saveToDatabase(connection: Connection, data: Collection<PlayerDataImpl?>)
    {
        this.trySave(connection, data, { d, statement, i, last ->
            statement.setLong(i(), d.id)
            statement.setString(i(), d.characterName)
            statement.setString(i(), d.profession.id.toString())
            statement.setLong(i(), d.xp)
            statement.setInt(i(), d.level)
            statement.setString(i(), d.location.town!!.id)
            statement.setInt(i(), d.location.x)
            statement.setInt(i(), d.location.y)
            statement.setInt(i(), d.baseStrength)
            statement.setInt(i(), d.baseAgility)
            statement.setInt(i(), d.baseIntellect)
            statement.setInt(i(), d.statPoints)
            statement.setLong(i(), d.gold)
            statement.setInt(i(), d.ttl)
            statement.setDate(i(), if(d.deadUntil == null) null else java.sql.Date(d.deadUntil!!.time))
            databaseManager.playerInventoryCache.saveOne(d.inventory!!)
        })
    }

    override fun canWipe(data: PlayerDataImpl): Boolean
    {
        return data.player_ != null && !data.player.online
    }
}