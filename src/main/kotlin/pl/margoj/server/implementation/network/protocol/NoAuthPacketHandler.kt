package pl.margoj.server.implementation.network.protocol

class NoAuthPacketHandler(private val manager: NetworkManager) : PacketHandler
{
    override fun handle(packet: IncomingPacket, out: OutgoingPacket)
    {
        if("getvar_addon" == packet.type && packet.queryParams.containsKey("callback"))
        {
            out.raw = packet.queryParams["callback"] + "(\"\")"
        }
        else
        {
            out.addAlert("<a href=\"generateauth.html\">Generuj ID</a>").addEngineAction(OutgoingPacket.EngineAction.STOP)
        }
    }

    override fun disconnect(reason: String)
    {
    }
}