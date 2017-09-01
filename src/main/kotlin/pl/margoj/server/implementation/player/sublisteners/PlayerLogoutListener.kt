package pl.margoj.server.implementation.player.sublisteners

import pl.margoj.server.implementation.network.protocol.IncomingPacket
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.player.PlayerConnection

class PlayerLogoutListener(connection: PlayerConnection) : PlayerPacketSubListener
(
        connection,
        onlyOnPlayer = true,
        onlyWithType = "logoff"
)
{
    override fun handle(packet: IncomingPacket, out: OutgoingPacket, query: Map<String, String>): Boolean
    {
        when (query["a"])
        {
            "start" ->
            {
                out.addJavascriptCode("location.href = 'logout'")
                out.addEngineAction(OutgoingPacket.EngineAction.RELOAD)
            }
            else ->
            {
                this.reportMaliciousData("invalid logoff action")
            }
        }
        return true
    }
}