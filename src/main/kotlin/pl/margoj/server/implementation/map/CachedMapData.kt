package pl.margoj.server.implementation.map

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import pl.margoj.mrf.map.metadata.pvp.MapPvP
import pl.margoj.mrf.map.metadata.welcome.WelcomeMessage
import pl.margoj.mrf.map.objects.gateway.GatewayObject
import pl.margoj.server.implementation.network.protocol.jsons.TownObject
import pl.margoj.server.implementation.utils.GsonUtils

class CachedMapData(val town: TownImpl)
{
    lateinit var townObject: TownObject
        private set

    lateinit var townElement: JsonElement
        private set

    lateinit var gw2: JsonArray
        private set

    lateinit var townname: JsonObject
        private set

    fun update()
    {
        this.townObject = TownObject(
                mapId = town.numericId,
                mainMapId = if (town.isMain) 0 else town.parentMap!!.numericId,
                width = town.width,
                height = town.height,
                imageFileName = "${town.id}.png",
                mapName = town.name,
                pvp = town.getMetadata(MapPvP::class.java).margonemId,
                water = "",
                battleBackground = "aa1.jpg",
                welcomeMessage = town.getMetadata(WelcomeMessage::class.java).value
        )

        this.townElement = GsonUtils.gson.toJsonTree(this.townObject)

        this.gw2 = JsonArray()
        this.townname = JsonObject()

        for (mapObject in town.objects)
        {
            val gateway = mapObject as? GatewayObject ?: continue
            val targetMap = town.server.getTownById(gateway.targetMap) ?: continue

            this.gw2.add(targetMap.numericId)
            this.gw2.add(gateway.position.x)
            this.gw2.add(gateway.position.y)
            this.gw2.add(0) // TODO needs key
            this.gw2.add(0) // TODO: min level & max level

            this.townname.add(targetMap.numericId.toString(), JsonPrimitive(targetMap.name))
        }
    }
}