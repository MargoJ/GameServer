package pl.margoj.server.implementation.network.protocol

interface PacketHandler
{
    fun handle(packet: IncomingPacket, out: OutgoingPacket)

    fun disconnect(reason: String)
}