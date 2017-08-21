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

class PlayerPreMovementPacketListener(connection: PlayerConnection) : PlayerPacketSubListener(connection, onlyOnPlayer = true)
{
    override fun handle(packet: IncomingPacket, out: OutgoingPacket, query: Map<String, String>): Boolean
    {
        val pdir = query["pdir"]
        if (pdir != null)
        {
            val intDirection = Parse.parseInt(pdir)
            this.checkForMaliciousData(intDirection == null || intDirection < 0 || intDirection > 3, "invalid direction")
            player!!.movementManager.playerDirection = intDirection!!
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
                catch (e: NumberFormatException)
                {
                    timestamp = null
                }

                this.checkForMaliciousData(x == null || y == null || timestamp == null, "invalid move format")

                player!!.movementManager.queueMove(x!!, y!!, timestamp!!)
            }

            player!!.movementManager.processMove()
        }

        if (packet.type == "walk")
        {
            val gateway = (player!!.location.town as? TownImpl)?.getObject(Point(player!!.location.x, player!!.location.y)) as? GatewayObject
            if (gateway != null)
            {
                val targetMap = player!!.server.getTownById(gateway.targetMap)

                if (targetMap == null || !targetMap.inBounds(gateway.target))
                {
                    player!!.logToConsole("unknown or invalid map: ${gateway.targetMap}")
                    player!!.server.logger.warn("unknown or invalid map: ${gateway.targetMap} at ${gateway.position}")
                }
                else
                {
                    player!!.teleport(Location(targetMap, gateway.target.x, gateway.target.y))
                }
            }
        }

        return true
    }
}