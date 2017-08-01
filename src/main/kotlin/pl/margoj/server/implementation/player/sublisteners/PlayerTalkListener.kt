package pl.margoj.server.implementation.player.sublisteners

import pl.margoj.server.implementation.network.protocol.IncomingPacket
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.player.PlayerConnection

class PlayerTalkListener(connection: PlayerConnection) : PlayerPacketSubListener
(
        connection,
        onlyOnPlayer = true,
        onlyWithType = "talk"
)
{
    override fun handle(packet: IncomingPacket, out: OutgoingPacket, query: Map<String, String>): Boolean
    {
        val talk = this.player!!.currentNpcTalk
        if(talk == null)
        {
            return true
        }

        val optionId = query["c"]?.toInt()
        this.checkForMaliciousData(optionId == null, "option not present")
        optionId!!

        for (option in talk.options)
        {
            if(option.id == optionId)
            {
                talk.update(option.label)
                return true
            }
        }

        return true
    }
}