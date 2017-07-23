package pl.margoj.server.implementation.player.sublisteners

import pl.margoj.server.implementation.network.protocol.IncomingPacket
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.player.PlayerConnection

class PlayerMovementSendListener(connection: PlayerConnection) : PlayerPacketSubListener(connection, onlyOnPlayer = true, async = true)
{
    override fun handle(packet: IncomingPacket, out: OutgoingPacket, query: Map<String, String>): Boolean
    {
        val move = player!!.movementManager.getNextMoveAndClear()

        if (move != null)
        {
            player!!.movementManager.updatePosition()
        }

        out.markAsOk()
        return true
    }
}