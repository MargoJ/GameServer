package pl.margoj.server.implementation.commands.defaults.admin

import pl.margoj.server.api.commands.Arguments
import pl.margoj.server.api.commands.CommandListener
import pl.margoj.server.api.commands.CommandSender

class ListCommand : CommandListener
{
    override fun commandPerformed(command: String, sender: CommandSender, args: Arguments)
    {
        sender.sendMessage("Lista graczy na serwerze: ")
        for (player in sender.server.players)
        {
            sender.sendMessage(" - ${player.name}")
        }
    }
}