package pl.margoj.server.implementation.player.sublisteners

import pl.margoj.server.implementation.network.protocol.IncomingPacket
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.player.PlayerConnection

class PlayerEventPacketListener(connection: PlayerConnection) : PlayerPacketSubListener(connection, async = true)
{
    override fun handle(packet: IncomingPacket, out: OutgoingPacket, query: Map<String, String>): Boolean
    {
        if (query.containsKey("ev"))
        {
            out.addEvent(this.connection.lastEvent)
        }

        return true
    }
}