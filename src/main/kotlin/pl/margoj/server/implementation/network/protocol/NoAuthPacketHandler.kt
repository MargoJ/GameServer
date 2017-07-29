package pl.margoj.server.implementation.network.protocol

import pl.margoj.server.implementation.network.http.HttpResponse

class NoAuthPacketHandler(private val manager: NetworkManager) : PacketHandler
{
    override fun handle(response: HttpResponse, packet: IncomingPacket, out: OutgoingPacket, callback: (OutgoingPacket) -> Unit)
    {
        if ("getvar_addon" == packet.type && packet.queryParams.containsKey("callback"))
        {
            out.raw = packet.queryParams["callback"] + "(\"\")"
        }
        else
        {
            out.addAlert("<a href=\"/login\">Zaloguj sie</a>").addEngineAction(OutgoingPacket.EngineAction.STOP)
        }

        callback(out)
    }

    override fun disconnect(reason: String)
    {
    }
}