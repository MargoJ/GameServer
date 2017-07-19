package pl.margoj.server.implementation.network.protocol

import pl.margoj.server.implementation.network.http.HttpResponse

interface PacketHandler
{
    fun handle(response: HttpResponse, packet: IncomingPacket, out: OutgoingPacket, callback: (OutgoingPacket) -> Unit)

    fun disconnect(reason: String)
}