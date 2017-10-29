package pl.margoj.server.implementation.player.sublisteners

import pl.margoj.mrf.map.Point
import pl.margoj.mrf.map.objects.gateway.GatewayObject
import pl.margoj.server.api.map.Location
import pl.margoj.server.api.utils.Parse
import pl.margoj.server.api.utils.splitByChar
import pl.margoj.server.implementation.map.TownImpl
import pl.margoj.server.implementation.network.protocol.IncomingPacket
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.player.PlayerConnection
import pl.margoj.server.implementation.player.PlayerImpl

class PlayerPreMovementPacketListener(connection: PlayerConnection) : PlayerPacketSubListener(connection, onlyOnPlayer = true)
{
    override fun handle(packet: IncomingPacket, out: OutgoingPacket, query: Map<String, String>): Boolean
    {
        val player = player!!
        val pdir = query["pdir"]
        val ml = query["ml"]
        val mts = query["mts"]

        if (pdir != null && ml == null && mts == null)
        {
            val intDirection = Parse.parseInt(pdir)
            this.checkForMaliciousData(intDirection == null || intDirection < 0 || intDirection > 3, "invalid direction")
            player.movementManager.playerDirection = intDirection!!
        }

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
                catch (e: NumberFormatException)
                {
                    timestamp = null
                }

                this.checkForMaliciousData(x == null || y == null || timestamp == null, "invalid move format")

                player.movementManager.queueMove(x!!, y!!, timestamp!!)
            }

            player.movementManager.processMove()
        }

        if (packet.type == "walk")
        {
            this.handleWalk(player)
        }

        return true
    }

    private fun handleWalk(player: PlayerImpl)
    {
        if (!player.movementManager.canMove)
        {
            return
        }

        val gateway = (player.location.town as? TownImpl)?.getObject(Point(player.location.x, player.location.y)) as? GatewayObject ?: return
        val targetMap = player.server.getTownById(gateway.targetMap)

        if (targetMap == null || !targetMap.inBounds(gateway.target))
        {
            player.logToConsole("unknown or invalid map: ${gateway.targetMap}")
            player.server.logger.warn("unknown or invalid map: ${gateway.targetMap} at ${gateway.position}")
            return
        }

        val levelRestriction = gateway.levelRestriction
        if (levelRestriction.enabled)
        {
            if (player.data.level < levelRestriction.minLevel || player.data.level > levelRestriction.maxLevel)
            {
                val message = StringBuilder("Przejście dostępne jest ")
                if (levelRestriction.minLevel != 0)
                {
                    message.append("od ").append(levelRestriction.minLevel).append(" ")
                }
                if (levelRestriction.maxLevel <= 1000)
                {
                    message.append("do ").append(levelRestriction.maxLevel).append(" ")
                }
                message.append("poziomu!")

                player.displayAlert(message.toString())
                return
            }
        }

        if (gateway.keyId != null)
        {
            val keyItem = this.server.getItemById(gateway.keyId!!)
            if (keyItem == null || !player.inventory.contains(keyItem))
            {
                player.displayAlert("Nie możesz przejść bez klucza")
                return
            }
        }

        val target = Location(targetMap, gateway.target.x, gateway.target.y)
        player.server.gameLogger.info("${player.name}: przejscie z ${player.location.toSimpleString()} do ${target.toSimpleString()})")
        player.teleport(target)
    }
}