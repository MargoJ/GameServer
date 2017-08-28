package pl.margoj.server.implementation.player.sublisteners

import pl.margoj.server.implementation.battle.ability.NormalStrike
import pl.margoj.server.implementation.battle.ability.Step
import pl.margoj.server.implementation.network.protocol.IncomingPacket
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.player.PlayerConnection

class PlayerBattleListener(connection: PlayerConnection) : PlayerPacketSubListener(connection, onlyWithType = "fight", onlyOnPlayer = true)
{
    override fun handle(packet: IncomingPacket, out: OutgoingPacket, query: Map<String, String>): Boolean
    {
        val player = player!!

        if (player.currentBattle == null)
        {
            return true
        }

        when (query["a"])
        {
            "quit" ->
            {
                if (!player.currentBattle!!.finished)
                {
                    player.displayAlert("Musisz poczekać do końca bitwy!")
                    return true
                }

                player.battleData!!.quitRequested = true
                player.currentBattle = null
            }
            "f" ->
            {
                player.battleData!!.auto = true
                player.battleData!!.needsAutoUpdate = true
            }
            "strike" ->
            {
                val targetId = query["id"]?.toLongOrNull() ?: return true
                val target = player.currentBattle!!.findById(targetId) ?: return true

                val ability = NormalStrike(player.currentBattle!!, player, target)
                ability.queue()
            }
            "move" ->
            {
                val ability = Step(player.currentBattle!!, player, player)
                ability.queue()
            }
            else -> player.displayAlert("nie wiem co probujesz zrobic ale to jeszcze nie zaimplementowane xD")
        }

        return true
    }
}