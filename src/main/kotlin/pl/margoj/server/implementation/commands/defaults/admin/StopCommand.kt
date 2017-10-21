package pl.margoj.server.implementation.commands.defaults.admin

import pl.margoj.server.api.commands.Arguments
import pl.margoj.server.api.commands.CommandListener
import pl.margoj.server.api.commands.CommandSender
import pl.margoj.server.api.player.Player
import pl.margoj.server.api.player.PlayerRank

class StopCommand : CommandListener
{
    override fun commandPerformed(command: String, sender: CommandSender, args: Arguments)
    {
        if(sender is Player && sender.rank != PlayerRank.ADMINISTRATOR)
        {
            sender.logToConsole("???", CommandSender.MessageSeverity.ERROR)
            return
        }

        sender.server.gameLogger.info("${sender.name}: .stop")
        sender.server.shutdown()
    }
}