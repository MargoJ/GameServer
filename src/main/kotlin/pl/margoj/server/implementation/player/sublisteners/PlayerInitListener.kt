package pl.margoj.server.implementation.player.sublisteners

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import pl.margoj.mrf.map.metadata.pvp.MapPvP
import pl.margoj.mrf.map.metadata.welcome.WelcomeMessage
import pl.margoj.mrf.map.objects.gateway.GatewayObject
import pl.margoj.server.api.utils.Parse
import pl.margoj.server.api.utils.TimeUtils
import pl.margoj.server.implementation.map.TownImpl
import pl.margoj.server.implementation.network.protocol.IncomingPacket
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.network.protocol.jsons.TownObject
import pl.margoj.server.implementation.player.PlayerConnection
import pl.margoj.server.implementation.player.PlayerImpl
import pl.margoj.server.implementation.utils.GsonUtils

class PlayerInitListener(connection: PlayerConnection) : PlayerPacketSubListener(connection, onlyWithType = "init")
{
    override fun handle(packet: IncomingPacket, out: OutgoingPacket, query: Map<String, String>): Boolean
    {
        val initlvl = Parse.parseInt(query["initlvl"])
        this.checkForMaliciousData(initlvl == null || initlvl !in 1..4, "invalid initlvl: ${query["initlvl"]}")

        connection.manager.server.logger.trace("handleInit, initlvl=$initlvl, aid=${connection.aid}")

        val gson = GsonUtils.gson

        when (initlvl)
        {
            1 ->
            {
                val j = out.json

                if (this.player == null)
                {
                    val player = PlayerImpl(this.connection.aid, "aid${this.connection.aid}", this.server, this.connection)
                    this.connection.player = player

                    this.server.entityManager.registerEntity(this.connection.player!!)

                    val location = player.location
                    location.town = this.server.getTownById("pierwsza_mapa") // TODO
                    location.x = 8
                    location.y = 13

                    player.connected()
                }
                else
                {
                    this.player!!.entityTracker.reset()
                    this.player!!.itemTracker.reset()
                }

                val town = this.player!!.location.town!! as TownImpl

                j.add("town", gson.toJsonTree(TownObject(
                        mapId = town.numericId,
                        mainMapId = 0,
                        width = town.width,
                        height = town.height,
                        imageFileName = "${town.id}.png",
                        mapName = town.name,
                        pvp = town.getMetadata(MapPvP::class.java).margonemId,
                        water = "",
                        battleBackground = "aa1.jpg",
                        welcomeMessage = town.getMetadata(WelcomeMessage::class.java).value
                )))

                val gw2 = JsonArray()
                val townname = JsonObject()

                for (mapObject in town.objects)
                {
                    val gateway = mapObject as? GatewayObject ?: continue
                    val targetMap = player!!.server.getTownById(gateway.targetMap) ?: continue

                    gw2.add(targetMap.numericId)
                    gw2.add(gateway.position.x)
                    gw2.add(gateway.position.y)
                    gw2.add(0) // TODO needs key
                    gw2.add(0) // TODO: min level & max level

                    townname.add(targetMap.numericId.toString(), JsonPrimitive(targetMap.name))
                }

                j.add("gw2", gw2)
                j.add("townname", townname)
                j.addProperty("worldname", this.server.config.serverConfig!!.name)
                j.addProperty("time", TimeUtils.getTimestampLong())
                j.addProperty("tutorial", -1)
                j.addProperty("clientver", 1461248638)

                j.add("h", gson.toJsonTree(this.player!!.data.createHeroObject()))
            }
            2 -> // collisions
            {
                out.json.addProperty("cl", (this.player!!.location.town!! as TownImpl).margonemCollisionsString)
            }
            3 -> // items
            {
                this.player!!.itemTracker.doTrack()
                this.connection.packetModifiers.forEach { it(out) }
            }
            4 -> // finish
            {
                out.addEvent()
            }
        }

        out.markAsOk()
        return true
    }
}