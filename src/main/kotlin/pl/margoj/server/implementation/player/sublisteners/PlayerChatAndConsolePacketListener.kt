package pl.margoj.server.implementation.player.sublisteners

import pl.margoj.server.implementation.network.protocol.IncomingPacket
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.player.PlayerConnection

class PlayerChatAndConsolePacketListener(connection: PlayerConnection) : PlayerPacketSubListener(connection, onlyOnPlayer = true)
{
    override fun handle(packet: IncomingPacket, out: OutgoingPacket, query: Map<String, String>): Boolean
    {
        if (packet.type == "chat")
        {
            val c = packet.body["c"] ?: query["c"]
            this.checkForMaliciousData(c == null, "no chat message present")
            this.connection.manager.server.chatManager.handle(player!!, c!!)
        }

        if (packet.type == "console")
        {
            val custom = query["custom"]
            this.checkForMaliciousData(custom == null, "no command provided")
            this.connection.manager.server.commandsManager.dispatchCommand(player!!, custom!!)
        }

        return true
    }
}