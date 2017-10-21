package pl.margoj.server.implementation.commands.defaults.debug

import pl.margoj.server.api.commands.Arguments
import pl.margoj.server.api.commands.CommandListener
import pl.margoj.server.api.commands.CommandSender
import pl.margoj.server.implementation.battle.buff.TestBuff
import pl.margoj.server.implementation.player.PlayerImpl

class TestBattleCommand : CommandListener
{
    override fun commandPerformed(command: String, sender: CommandSender, args: Arguments)
    {
        sender as PlayerImpl
        sender.battleData!!.addBuff(TestBuff(), 2)
    }
}