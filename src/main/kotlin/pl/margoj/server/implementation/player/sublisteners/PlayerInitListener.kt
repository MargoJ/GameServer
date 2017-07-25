package pl.margoj.server.implementation.player.sublisteners

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import pl.margoj.mrf.map.metadata.pvp.MapPvP
import pl.margoj.mrf.map.metadata.welcome.WelcomeMessage
import pl.margoj.mrf.map.objects.gateway.GatewayObject
import pl.margoj.server.api.events.player.PlayerJoinEvent
import pl.margoj.server.api.utils.Parse
import pl.margoj.server.api.utils.TimeUtils
import pl.margoj.server.implementation.map.TownImpl
import pl.margoj.server.implementation.network.protocol.IncomingPacket
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.network.protocol.jsons.TownObject
import pl.margoj.server.implementation.player.PlayerConnection
import pl.margoj.server.implementation.player.PlayerDataImpl
import pl.margoj.server.implementation.player.PlayerImpl
import pl.margoj.server.implementation.player.StatisticType
import pl.margoj.server.implementation.utils.GsonUtils

class PlayerInitListener(connection: PlayerConnection) : PlayerPacketSubListener(connection, onlyWithType = "init", async = true)
{
    override fun handle(packet: IncomingPacket, out: OutgoingPacket, query: Map<String, String>): Boolean
    {
        val initlvl = Parse.parseInt(query["initlvl"])
        this.checkForMaliciousData(initlvl == null || initlvl !in 1..4, "invalid initlvl: ${query["initlvl"]}")

        connection.manager.server.logger.trace("handleInit, initlvl=$initlvl, aid=${connection.aid}")

        if (initlvl != 1 && player == null)
        {
            out.addAlert("Nie jesteś zalogowany!")
            out.addEngineAction(OutgoingPacket.EngineAction.STOP)
            return false
        }

        when (initlvl)
        {
            1 ->
            {
                if (this.player == null)
                {
                    val data = server.databaseManager.playerDataCache.loadOne(connection.aid.toLong())

                    if (data == null)
                    {
                        out.addAlert("Nie jesteś zalogowany!")
                        out.addEngineAction(OutgoingPacket.EngineAction.STOP)
                        return false
                    }

                    server.ticker.registerWaitable { this.handleNewPlayer(data) }.wait()
                }
                else
                {
                    server.ticker.registerWaitable { this.handleOnlinePlayer() }.wait()
                }

                server.ticker.registerWaitable { this.handleInit(out) }.wait()
            }
            2 -> // collisions
            {
                server.ticker.registerWaitable { out.json.addProperty("cl", (this.player!!.location.town!! as TownImpl).margonemCollisionsString) }.wait()
            }
            3 -> // items
            {
                server.ticker.registerWaitable { this.initItems() }.wait()
            }
            4 -> // finish
            {
                out.addEvent()
            }
        }

        out.markAsOk()
        return true
    }

    private fun handleNewPlayer(data: PlayerDataImpl)
    {
        val player = PlayerImpl(data, this.server, this.connection)
        data.player_ = player
        data.inventory!!.player_ = player
        this.connection.player = player

        this.server.entityManager.registerEntity(this.connection.player!!)

        val location = player.location
        if(location.town == null)
        {
            location.town = this.server.getTownById("pierwsza_mapa") // TODO
            location.x = 8
            location.y = 13
        }

        player.server.eventManager.call(PlayerJoinEvent(player))
        player.connected()
    }

    private fun handleOnlinePlayer()
    {
        this.player!!.entityTracker.reset()

        val tracker = this.player!!.itemTracker
        tracker.enabled = false
        tracker.reset()
    }

    private fun handleInit(out: OutgoingPacket)
    {
        val j = out.json

        val town = this.player!!.location.town!! as TownImpl

        j.add("town", GsonUtils.gson.toJsonTree(TownObject(
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

        j.add("h", GsonUtils.gson.toJsonTree(this.player!!.data.recalculateStatistics(StatisticType.ALL)))
    }

    private fun initItems()
    {
        val tracker = this.player!!.itemTracker
        tracker.enabled = true
        tracker.reset()
        tracker.doTrack()
    }
}