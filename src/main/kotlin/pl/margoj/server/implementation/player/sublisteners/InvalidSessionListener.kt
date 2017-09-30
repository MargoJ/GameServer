package pl.margoj.server.implementation.player.sublisteners

import pl.margoj.server.implementation.network.protocol.IncomingPacket
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.player.PlayerConnection

class InvalidSessionListener(connection: PlayerConnection) : PlayerPacketSubListener(connection)
{
    override fun handle(packet: IncomingPacket, out: OutgoingPacket, query: Map<String, String>): Boolean
    {
        if (this.connection.authSession.invalidated)
        {
            this.connection.disconnect("Sesja wygasła")
            return false
        }

        return true
    }
}