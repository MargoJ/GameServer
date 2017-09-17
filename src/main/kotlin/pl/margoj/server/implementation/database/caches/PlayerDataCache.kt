package pl.margoj.server.implementation.database.caches

import pl.margoj.server.api.map.Location
import pl.margoj.server.api.player.Gender
import pl.margoj.server.api.player.Profession
import pl.margoj.server.api.utils.Parse
import pl.margoj.server.implementation.database.DatabaseManager
import pl.margoj.server.implementation.database.DatabaseObjectCache
import pl.margoj.server.implementation.database.TableNames
import pl.margoj.server.implementation.player.PlayerDataImpl
import pl.margoj.server.implementation.player.options.PlayerOptions
import java.sql.Connection
import java.sql.PreparedStatement

class PlayerDataCache(databaseManager: DatabaseManager) : DatabaseObjectCache<Long, PlayerDataImpl>
(
        databaseManager,
        TableNames.PLAYERS,
        rawColumns = *arrayOf(
                "id", "characterName", "profession", "gender", "experience", "level", "hp", "map", "x", "y", "baseStrength", "baseAgility", "baseIntellect", "statPoints",
                "gold", "ttl", "dead_until", "player_options"
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
            data.gender = Gender.getById(it.getString("gender")[0])!!
            data.xp = it.getLong("experience")
            data.level = it.getInt("level")
            data.hp = it.getInt("hp")
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
            data.deadUntil = it.getTimestamp("dead_until")
            data.inventory = databaseManager.playerInventoryCache.loadOne(data.id)
            data.playerOptions = PlayerOptions(it.getInt("player_options"))
            data
        }
    }

    override fun saveToDatabase(connection: Connection, data: Collection<PlayerDataImpl?>)
    {
        this.trySave(connection, data, { d, statement, i, last ->
            statement.setLong(i(), d.id)
            statement.setString(i(), d.characterName)
            statement.setString(i(), d.profession.id.toString())
            statement.setString(i(), d.gender.id.toString())
            statement.setLong(i(), d.xp)
            statement.setInt(i(), d.level)
            statement.setInt(i(), d.hp)
            statement.setString(i(), d.location.town!!.id)
            statement.setInt(i(), d.location.x)
            statement.setInt(i(), d.location.y)
            statement.setInt(i(), d.baseStrength)
            statement.setInt(i(), d.baseAgility)
            statement.setInt(i(), d.baseIntellect)
            statement.setInt(i(), d.statPoints)
            statement.setLong(i(), d.gold)
            statement.setInt(i(), d.ttl)
            statement.setTimestamp(i(), if (d.deadUntil == null) null else java.sql.Timestamp(d.deadUntil!!.time))
            statement.setInt(i(), d.playerOptions.intValue)
            databaseManager.playerInventoryCache.saveOne(d.inventory!!)
        })
    }

    override fun canWipe(data: PlayerDataImpl): Boolean
    {
        return data.player_ != null && !data.player.online
    }
}