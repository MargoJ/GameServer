package pl.margoj.server.implementation.commands.defaults.debug

import pl.margoj.server.api.commands.Arguments
import pl.margoj.server.api.commands.CommandListener
import pl.margoj.server.api.commands.CommandSender
import pl.margoj.server.api.player.Player
import pl.margoj.server.implementation.battle.BattleImpl
import pl.margoj.server.implementation.entity.EntityImpl
import pl.margoj.server.implementation.npc.Npc
import pl.margoj.server.implementation.npc.NpcType
import pl.margoj.server.implementation.player.PlayerImpl
import java.util.Collections

class TestBattleCommand : CommandListener
{
    override fun commandPerformed(command: String, sender: CommandSender, args: Arguments)
    {
        args.ensureTrue({ sender is Player }, "Tylko gracz może wykonać tą komende")

        val enemy = (if (args.has(0))
        {
            args.asPlayer(0)
        }
        else
        {
            sender.server.entityManager.entities.filter { it is Npc && it.type == NpcType.NPC }[1]
        }) as? EntityImpl?


        sender as PlayerImpl
        sender.hp = sender.stats.maxHp

        if(enemy is Player)
        {
            enemy.hp = enemy.stats.maxHp
        }

        args.ensureNotNull(enemy, "Nie znaleziono przeciwnika")
        enemy!!

        val battle = BattleImpl(
                server = sender.server,
                teamA = Collections.singletonList(sender),
                teamB = Collections.singletonList(enemy)
        )

        battle.start()
    }
}