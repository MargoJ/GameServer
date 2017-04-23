package pl.margoj.server.implementation.player

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import pl.margoj.server.api.utils.Parse
import pl.margoj.server.api.utils.TimeUtils
import pl.margoj.server.api.utils.splitByChar
import pl.margoj.server.implementation.map.TownImpl
import pl.margoj.server.implementation.network.protocol.IncomingPacket
import pl.margoj.server.implementation.network.protocol.NetworkManager
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.network.protocol.PacketHandler
import pl.margoj.server.implementation.network.protocol.jsons.TownObject
import pl.margoj.server.implementation.utils.GsonUtils
import java.util.concurrent.CopyOnWriteArrayList

class PlayerConnection(val manager: NetworkManager, val aid: Int) : PacketHandler
{
    private val logger = manager.server.logger
    private val packetModifiers = CopyOnWriteArrayList<(OutgoingPacket) -> Unit>()
    private var disconnectReason: String? = null
    private var disposed: Boolean = false

    var lastPacket: Long = 0
    var ip: String? = null
    var player: PlayerImpl? = null
        private set

    init
    {
        logger.trace("New connection object create for aid=$aid")
    }

    override fun handle(packet: IncomingPacket, out: OutgoingPacket)
    {
        this.lastPacket = System.currentTimeMillis()
        val query = packet.queryParams

        if (this.disposed)
        {
            out.addEngineAction(OutgoingPacket.EngineAction.RELOAD)
            return
        }

        if (this.disconnectReason != null)
        {
            out.addEngineAction(OutgoingPacket.EngineAction.STOP)
            out.addWarn(this.disconnectReason!!)
            this.disconnectReason = null
            return
        }

        if (packet.type == "init")
        {
            val initlvl = Parse.parseInt(query["initlvl"])
            if (initlvl != null)
            {
                this.handleInit(initlvl, out)
            }
        }

        if (this.player != null)
        {
            this.handlePlayer(this.player!!, packet, out)
        }

        if (query.containsKey("ev"))
        {
            out.addEvent()
        }

        this.packetModifiers.forEach { it(out) }

        this.packetModifiers.clear()
    }

    private fun handleInit(initlvl: Int, out: OutgoingPacket)
    {
        logger.trace("handleInit, initlvl=$initlvl, aid=$aid")
        val gson = GsonUtils.gson

        when (initlvl)
        {
            1 ->
            {
                val j = out.json

                if (this.player == null)
                {
                    this.player = PlayerImpl(this.aid, "aid$aid", this.manager.server, this)
                    this.manager.server.entityManager.registerEntity(player!!)
                    val location = this.player!!.location
                    location.town = this.manager.server.getTownById("pierwsza_mapa") // TODO
                    location.x = 8
                    location.y = 13
                }
                else
                {
                    this.player!!.entityTracker.reset()
                }

                val town = this.player!!.location.town!!

                j.add("town", gson.toJsonTree(TownObject(
                        mapId = 1,
                        mainMapId = 0,
                        width = town.width,
                        height = town.height,
                        imageFileName = "${town.id}.png",
                        mapName = town.name,
                        pvp = 2,
                        water = "",
                        battleBackground = "aa1.jpg",
                        welcomeMessage = ""
                )))

                j.add("gw2", JsonArray())
                j.add("townname", JsonObject())
                j.addProperty("worldname", this.manager.server.config.serverConfig!!.name)
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
                out.json.add("item", JsonObject())
            }
            4 -> // finish
            {
                out.addEvent()
            }
        }

        out.markAsOk()
    }


    private fun handlePlayer(player: PlayerImpl, packet: IncomingPacket, out: OutgoingPacket)
    {
        val query = packet.queryParams

        // handle direction, has to be handled before movemenet
        val pdir = query["pdir"]
        if (pdir != null)
        {
            val intDirection = Parse.parseInt(pdir)
            this.checkForMaliciousData(intDirection == null || intDirection < 0 || intDirection > 3, "invalid direction")
            player.movementManager.playerDirection = intDirection!!
        }

        val ml = query["ml"]
        val mts = query["mts"]

        if (ml != null && mts != null)
        {
            val moveList = ml.splitByChar(';')
            val moveTimestamps = mts.splitByChar(';')

            this.checkForMaliciousData(moveList.isEmpty() || moveList.size != moveTimestamps.size, "ml.size() != mts.size()")

            for (i in 0..(moveList.size - 1))
            {
                val move = moveList[i]
                val moveSplit = move.splitByChar(',')
                this.checkForMaliciousData(moveSplit.size != 2, "invalid move format")

                val x = Parse.parseInt(moveSplit[0])
                val y = Parse.parseInt(moveSplit[1])
                var timestamp: Double?

                try
                {
                    timestamp = moveTimestamps[i].toDouble()
                }
                catch(e: NumberFormatException)
                {
                    timestamp = null
                }

                this.checkForMaliciousData(x == null || y == null || timestamp == null, "invalid move format")

                player.movementManager.queueMove(x!!, y!!, timestamp!!)
            }
        }

        if(packet.type == "chat")
        {
            val c = packet.body["c"]
            this.checkForMaliciousData(c == null, "no chat message present")
            this.manager.server.chatManager.handle(player, c!!)
        }

        if(packet.type == "console")
        {
            val custom = query["custom"]
            this.checkForMaliciousData(custom == null, "no command provided")
            this.manager.server.commandsManager.dispatchCommand(player, custom!!)
        }

        val move = player.movementManager.processMove()

        if(move != null)
        {
            out.addMove(move.x, move.y)
        }

        player.entityTracker.handlePacket(out)

        out.markAsOk()
    }

    override fun disconnect(reason: String)
    {
        this.disconnectReason = reason
    }

    fun addModifier(modifier: (OutgoingPacket) -> Unit)
    {
        this.packetModifiers.add(modifier)
    }

    fun dispose()
    {
        this.player = null
        this.disposed = true
    }

    private fun checkForMaliciousData(condition: Boolean, info: String)
    {
        if (condition)
        {
            this.disconnect("Malicious packet")
            throw IllegalArgumentException("Malicius packet: " + info)
        }
    }
}