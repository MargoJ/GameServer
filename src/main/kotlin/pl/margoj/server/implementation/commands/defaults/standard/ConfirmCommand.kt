package pl.margoj.server.implementation.commands.defaults.standard

import pl.margoj.server.api.commands.Arguments
import pl.margoj.server.api.commands.CommandListener
import pl.margoj.server.api.commands.CommandSender

class ConfirmCommand : CommandListener
{
    override fun commandPerformed(command: String, sender: CommandSender, args: Arguments)
    {
        if(!sender.executeConfirmationTask())
        {
            sender.sendMessage("Nie ma nic do potwierdzenia", CommandSender.MessageSeverity.WARN)
        }
    }
}