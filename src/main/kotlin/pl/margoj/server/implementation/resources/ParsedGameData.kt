package pl.margoj.server.implementation.resources

import pl.margoj.mrf.data.GameData
import pl.margoj.server.api.player.Profession
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.item.ItemImpl
import pl.margoj.server.implementation.map.TownImpl

data class ParsedGameData(val respawnMap: TownImpl, val professionRespawnMap: Map<Profession, TownImpl>, val defaultBag: ItemImpl)
{
    companion object
    {
        fun create(server: ServerImpl, data: GameData): ParsedGameData
        {
            val spawnMapId = data.spawns['d'] ?: throw IllegalArgumentException("no default map set")
            val defaultSpawn = server.getTownById(spawnMapId) ?: throw IllegalArgumentException("no default map found")

            val professionRespawnMap = HashMap<Profession, TownImpl>(6)

            for (profession in Profession.values())
            {
                val spawn = data.spawns[profession.id]
                if (spawn == null)
                {
                    server.logger.warn("Brak spawnu dla profesji ${profession.name}, uzywam domyslnego spawnu: ${defaultSpawn.name} [${defaultSpawn.id}]")
                    professionRespawnMap.put(profession, defaultSpawn)
                }
                else
                {
                    val spawnMap = server.getTownById(spawn)
                    if (spawnMap == null)
                    {
                        server.logger.warn("Spawn '$spawn' jest niepoprawny! Taka mapa nie istnieje, uzywam domyslnego spawnu dla profesji ${profession.name}")
                        professionRespawnMap.put(profession, defaultSpawn)
                    }
                    else
                    {
                        professionRespawnMap.put(profession, spawnMap)
                    }
                }
            }

            val defaultBag = server.getItemById(data.defaultBag) as? ItemImpl ?: throw IllegalArgumentException("no default bag set")

            return ParsedGameData(defaultSpawn, professionRespawnMap, defaultBag)
        }
    }
}