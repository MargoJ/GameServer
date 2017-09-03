package pl.margoj.server.implementation.player.sublisteners

import pl.margoj.server.implementation.network.protocol.IncomingPacket
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.npc.Npc
import pl.margoj.server.implementation.npc.NpcTalk
import pl.margoj.server.implementation.npc.NpcType
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
        if (query["c"] == null)
        {
            if (!player!!.movementManager.canMove)
            {
                return true
            }

            val id = query["id"]?.toInt()
            this.checkForMaliciousData(id == null, "invalid id")
            id!!

            val npc = this.player!!.server.entityManager.getNpcById(id)
            this.checkForMaliciousData(npc == null || npc.type != NpcType.NPC, "invalid npc")
            npc!!

            if (!player!!.location.isNear(npc.location, true))
            {
                out.addAlert("Jesteś zbyt daleko od postaci, by z nią rozmawiać!")
                return true
            }

            player!!.server.gameLogger.info("${player!!.name}: rozmowa z npc ${npc.id}[${npc.name}], lokacja= ${npc.location.toSimpleString()}")

            if (npc.script == null)
            {
                return true
            }

            val talk = NpcTalk(player!!, npc, npc.script)
            player!!.currentNpcTalk = talk
            return true
        }

        val talk = this.player!!.currentNpcTalk ?: return true

        val optionId = query["c"]?.toInt()
        this.checkForMaliciousData(optionId == null, "option not present")
        optionId!!

        for (option in talk.options)
        {
            if (option.id == optionId)
            {
                try
                {
                    talk.update(option.label)
                }
                catch (e: Exception)
                {
                    player!!.currentNpcTalk = null
                    throw e
                }

                return true
            }
        }

        return true
    }
}