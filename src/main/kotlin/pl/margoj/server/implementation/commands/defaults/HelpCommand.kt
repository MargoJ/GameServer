package pl.margoj.server.implementation.commands.defaults

import pl.margoj.server.api.commands.Arguments
import pl.margoj.server.api.commands.CommandListener
import pl.margoj.server.api.commands.CommandSender

class HelpCommand : CommandListener
{
    private val ENTRIES_PER_PAGE = 3

    override fun commandPerformed(command: String, sender: CommandSender, args: Arguments)
    {
        val page = (if (args.has(0)) args.asInt(0) else null) ?: 1

        val startIndex = (page - 1) * ENTRIES_PER_PAGE
        val endIndex = (page) * ENTRIES_PER_PAGE - 1
        val values = sender.server.commandsManager.getAllCommands().keys.sorted()
        val allPages = Math.ceil(values.size.toDouble() / ENTRIES_PER_PAGE.toDouble()).toInt()
        var any = false

        sender.sendMessage("Lista dostępnych komend ($page/$allPages): ")

        for (i in startIndex..endIndex)
        {
            if (i >= 0 && i < values.size)
            {
                any = true
                sender.sendMessage(".${values[i]}")
            }
        }

        if (!any)
        {
            sender.sendMessage("Nie znaleziono strony $page", CommandSender.MessageSeverity.WARN)
        }
        else
        {
            if (page != allPages)
            {
                sender.sendMessage("Aby zobaczyć kolejną strone użyj: .help ${page + 1}")
            }
        }
    }
}