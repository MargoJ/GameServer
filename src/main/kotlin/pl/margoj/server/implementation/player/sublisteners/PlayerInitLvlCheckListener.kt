package pl.margoj.server.implementation.player.sublisteners

import pl.margoj.server.implementation.network.protocol.IncomingPacket
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.player.PlayerConnection

class PlayerInitLvlCheckListener(connection: PlayerConnection) : PlayerPacketSubListener(connection)
{
    override fun handle(packet: IncomingPacket, out: OutgoingPacket, query: Map<String, String>): Boolean
    {
        if (connection.initLevel == 4)
        {
            return true
        }

        val expectedInitLevel = connection.initLevel + 1

        if (packet.type != "init")
        {
            this.info(out)
            return false
        }

        if (query["initlvl"]?.toInt() != expectedInitLevel)
        {
            this.info(out)
            return false
        }

        return true
    }

    private fun info(out: OutgoingPacket)
    {
        this.connection.initLevel = 0
        out.addEngineAction(OutgoingPacket.EngineAction.STOP)
        out.addWarn("Pominięto pakiet danych inicjalizujących, odświerz strone")
    }
}