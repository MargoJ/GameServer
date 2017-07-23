package pl.margoj.server.implementation.commands.defaults

import pl.margoj.server.api.commands.Arguments
import pl.margoj.server.api.commands.CommandListener
import pl.margoj.server.api.commands.CommandSender
import pl.margoj.server.api.player.Player

class AddXPCommand: CommandListener
{
    override fun commandPerformed(command: String, sender: CommandSender, args: Arguments)
    {
        args.ensureTrue({ args.has(0) }, "Prawidłowe użycie: .$command xp")
        args.ensureTrue({ sender is Player }, "Tylko gracz moze wykonać tą komende")
        sender as Player

        val xp = args.asLong(0)
        args.ensureNotNull(xp, "${args.asString(0)} nie jest poprawną liczbą")
        xp!!

        sender.data.addExp(xp)

        sender.sendMessage("Dodano $xp xp!")
    }

}