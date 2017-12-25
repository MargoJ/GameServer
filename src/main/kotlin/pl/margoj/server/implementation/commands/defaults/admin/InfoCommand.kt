package pl.margoj.server.implementation.commands.defaults.admin

import pl.margoj.server.api.commands.Arguments
import pl.margoj.server.api.commands.CommandListener
import pl.margoj.server.api.commands.CommandSender
import pl.margoj.server.api.player.Player
import pl.margoj.utils.commons.time.TimeFormatUtils

class InfoCommand : CommandListener
{
    override fun commandPerformed(command: String, sender: CommandSender, args: Arguments)
    {
        args.ensureTrue({ sender is Player || args.has(0) }, "Prawidłowe użycie: .info [gracz]")

        val target = if (args.has(0)) args.asPlayer(0) else (sender as Player)
        args.ensureNotNull(target, "Nie znaleziono podanego gracza")
        target!!

        sender.sendMessage("=== Informacje o graczu: ${target.name}")
        sender.sendMessage("ID: ${target.id}")
        sender.sendMessage("Nick: ${target.name}")
        sender.sendMessage("Level: ${target.level} [XP: ${target.data.xp}]")
        sender.sendMessage("Płeć: ${target.gender.id}")
        sender.sendMessage("Ikona: ${target.icon}")
        sender.sendMessage("Lokacja: ${target.location.toSimpleString()}")
        sender.sendMessage("HP: ${target.hp}/${target.stats.maxHp} [${target.healthPercent}%]")
        sender.sendMessage("Złoto: ${target.currencyManager.gold}/${target.currencyManager.goldLimit}")
        sender.sendMessage("Wyczerpanie: ${target.data.ttl}")

        if (target.data.isDead)
        {
            val left = target.data.deadUntil!!.time - System.currentTimeMillis()
            sender.sendMessage("Martwy: tak, pozostało ${TimeFormatUtils.getReadableTime(left)}")
        }
        else
        {
            sender.sendMessage("Martwy: nie")
        }
    }
}