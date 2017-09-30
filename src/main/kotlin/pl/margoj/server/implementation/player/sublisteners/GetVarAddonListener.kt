package pl.margoj.server.implementation.player.sublisteners

import pl.margoj.server.implementation.network.protocol.IncomingPacket
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.player.PlayerConnection

class GetVarAddonListener(connection: PlayerConnection) : PlayerPacketSubListener(connection, onlyWithType = "getvar_addon", async = true)
{
    override fun handle(packet: IncomingPacket, out: OutgoingPacket, query: Map<String, String>): Boolean
    {
        if(packet.queryParams.containsKey("callback"))
        {
            out.raw = packet.queryParams["callback"] + "(\"\")"
            return false
        }

        return true
    }
}