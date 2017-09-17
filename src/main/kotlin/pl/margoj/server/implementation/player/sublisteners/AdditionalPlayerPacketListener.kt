package pl.margoj.server.implementation.player.sublisteners

import pl.margoj.server.implementation.network.protocol.IncomingPacket
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.player.PlayerConnection
import pl.margoj.server.implementation.player.StatisticType

class AdditionalPlayerPacketListener(connection: PlayerConnection) : PlayerPacketSubListener(connection, onlyOnPlayer = true)
{
    override fun handle(packet: IncomingPacket, out: OutgoingPacket, query: Map<String, String>): Boolean
    {
        val player = this.player!!

        player.entityTracker.handlePacket(out)

        if (player.currentNpcTalk != null && player.currentNpcTalk!!.needsUpdate)
        {
            player.currentNpcTalk!!.needsUpdate = false
            player.currentNpcTalk!!.handlePacket(out)

            if (player.currentNpcTalk!!.finished)
            {
                player.currentNpcTalk = null
            }
        }

        if (player.data.isDead)
        {
            val left = player.data.deadUntil!!.time - System.currentTimeMillis()
            out.json.addProperty("dead", left / 1000L)
        }

        if(player.data.playerOptions.needsUpdate)
        {
            out.addStatisticRecalculation(StatisticType.OPTIONS)
        }

        return true
    }
}