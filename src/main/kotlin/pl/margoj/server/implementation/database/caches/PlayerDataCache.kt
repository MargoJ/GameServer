package pl.margoj.server.implementation.database.caches

import pl.margoj.server.api.map.Location
import pl.margoj.server.api.player.Profession
import pl.margoj.server.implementation.database.DatabaseManager
import pl.margoj.server.implementation.database.DatabaseObjectCache
import pl.margoj.server.implementation.database.TableNames
import pl.margoj.server.implementation.player.PlayerDataImpl
import java.sql.Connection

class PlayerDataCache(databaseManager: DatabaseManager) : DatabaseObjectCache<PlayerDataImpl>
(
        databaseManager,
        TableNames.PLAYERS,
        rawColumns = *arrayOf("id", "characterName", "profession", "experience", "level", "map", "x", "y", "baseStrength", "baseAgility", "baseIntellect", "statPoints")
)
{

    override fun getIdOf(data: PlayerDataImpl): Long
    {
        return data.id
    }

    override fun loadFromDatabase(connection: Connection, id: LongArray): List<PlayerDataImpl?>
    {
        return this.tryLoad(connection, id) {
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
            data.inventory = databaseManager.playerInventoryCache.loadOne(data.id)
            data
        }
    }

    override fun saveToDatabase(connection: Connection, data: Collection<PlayerDataImpl?>)
    {
        this.trySave(connection, data) { d, statement, i, last ->
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
            databaseManager.playerInventoryCache.saveOne(d.inventory!!)
        }
    }

    override fun canWipe(data: PlayerDataImpl): Boolean
    {
        return data.player_ != null && !data.player.online
    }
}