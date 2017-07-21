package pl.margoj.server.implementation.player.sublisteners

import pl.margoj.server.api.utils.TimeUtils
import pl.margoj.server.implementation.network.protocol.IncomingPacket
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.player.PlayerConnection

class PlayerEventCheckPacketListener(connection: PlayerConnection) : PlayerPacketSubListener(connection)
{
    override fun handle(packet: IncomingPacket, out: OutgoingPacket, query: Map<String, String>): Boolean
    {
        val ev = packet.queryParams["ev"]

        if (ev != null)
        {
            try
            {
                val evDouble = ev.toDouble()
                if (evDouble < this.connection.lastEvent)
                {
                    out.addWarn("Odrzucono stare zapytanie - nowsze już zostało przetworzone")
                    out.addEvent(this.connection.lastEvent)
                    out.markAsOk()
                    return false
                }
                this.connection.lastEvent = TimeUtils.getTimestampDouble()
            }
            catch(e: NumberFormatException)
            {
                this.reportMaliciousData("Invalid 'ev' received")
            }
        }

        return true
    }
}