package pl.margoj.server.implementation.player.sublisteners

import pl.margoj.server.api.battle.BattleUnableToStartException
import pl.margoj.server.implementation.battle.ability.Step
import pl.margoj.server.implementation.battle.ability.fight.NormalStrikeAbility
import pl.margoj.server.implementation.entity.LivingEntityImpl
import pl.margoj.server.implementation.network.protocol.IncomingPacket
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.npc.Npc
import pl.margoj.server.implementation.npc.NpcType
import pl.margoj.server.implementation.player.PlayerConnection

class PlayerBattleListener(connection: PlayerConnection) : PlayerPacketSubListener(connection, onlyWithType = "fight", onlyOnPlayer = true)
{
    override fun handle(packet: IncomingPacket, out: OutgoingPacket, query: Map<String, String>): Boolean
    {
        val player = player!!

        if (query["a"] == "attack")
        {
            val id = query["id"]?.toIntOrNull()
            this.checkForMaliciousData(id == null, "Invalid id")
            id!!

            val targetEntity: LivingEntityImpl?
            if (id < 0)
            {
                targetEntity = this.server.entityManager.getNpcById(-id)

                this.checkForMaliciousData(targetEntity is Npc && targetEntity.type != NpcType.MONSTER, "not a monster")
            }
            else
            {
                targetEntity = this.server.entityManager.getPlayerById(id)

                if(targetEntity == player)
                {
                    player.displayAlert("Chcesz stoczyć wewnętrzną walkę? xD") // <3
                    return true
                }
            }

            if (targetEntity == null || !player.location.isNear(targetEntity.location, true))
            {
                return true
            }

            val unavailabilityCause = targetEntity.battleUnavailabilityCause

            if (unavailabilityCause == BattleUnableToStartException.Cause.PLAYER_IS_OFFLINE)
            {
                return true
            }

            if (unavailabilityCause == BattleUnableToStartException.Cause.ENTITY_IS_DEAD)
            {
                player.displayAlert("Przeciwnik jest już martwy")
                return true
            }

            if (unavailabilityCause == BattleUnableToStartException.Cause.ENTITY_IN_BATTLE)
            {
                player.displayAlert("Przeciwnik walczy z kimś innym")
                return true
            }

            server.startBattle(player.withGroup, targetEntity.withGroup)
            return true
        }

        if (player.currentBattle == null)
        {
            return true
        }

        val battle = player.currentBattle!!
        val battleData = player.battleData!!

        when (query["a"])
        {
            "quit" ->
            {
                if (!battle.finished)
                {
                    player.displayAlert("Musisz poczekać do końca bitwy!")
                    return true
                }

                battleData.quitRequested = true
                player.currentBattle = null
            }
            "f" ->
            {
                battleData.auto = true
                battleData.needsAutoUpdate = true
            }
            "strike" ->
            {
                val targetId = query["id"]?.toIntOrNull() ?: return true
                val target = battle.findById(targetId) ?: return true

                val ability = NormalStrikeAbility(battle, player, target)
                ability.queue()
            }
            "move" ->
            {
                val ability = Step(battle, player, player)
                ability.queue()
            }
            else -> player.displayAlert("nie wiem co probujesz zrobic ale to jeszcze nie zaimplementowane xD")
        }

        return true
    }
}