package pl.margoj.server.implementation.commands.defaults.admin

import pl.margoj.server.api.commands.Arguments
import pl.margoj.server.api.commands.CommandListener
import pl.margoj.server.api.commands.CommandSender
import pl.margoj.server.api.player.Player

class AddXPCommand : CommandListener
{
    override fun commandPerformed(command: String, sender: CommandSender, args: Arguments)
    {
        args.ensureTrue({ args.has(0) }, "Prawidłowe użycie: .$command xp [gracz]")
        args.ensureTrue({ sender is Player || args.has(1) }, "Prawidłowe użycie: .$command xp gracz")

        val target = if (args.has(1)) args.asPlayer(1) else (sender as Player)
        args.ensureNotNull(target, "Nie znaleziono podanego gracza")
        target!!

        val xp = args.asLong(0)
        args.ensureTrue({ xp != null && target.data.xp + xp > target.data.xp }, "${args.asString(0)} nie jest poprawną liczbą")
        xp!!

        target.data.addExp(xp)
        sender.sendMessage("Dodano $xp xp!")
        sender.server.gameLogger.info("${sender.name}: .addxp: $xp ${target.name}")
    }
}