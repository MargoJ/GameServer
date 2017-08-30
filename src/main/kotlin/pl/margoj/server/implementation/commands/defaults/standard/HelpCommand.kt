package pl.margoj.server.implementation.commands.defaults.standard

import pl.margoj.server.api.commands.Arguments
import pl.margoj.server.api.commands.CommandListener
import pl.margoj.server.api.commands.CommandSender
import pl.margoj.server.api.utils.Paged

class HelpCommand : CommandListener
{
    private val ENTRIES_PER_PAGE = 8

    override fun commandPerformed(command: String, sender: CommandSender, args: Arguments)
    {
        val page = (if (args.has(0)) args.asInt(0) else null) ?: 1
        val paged = Paged.createListBasedPaged(sender.server.commandsManager.getAllCommands().keys.sorted(), ENTRIES_PER_PAGE)

        if (!paged.isThereAPage(page))
        {
            sender.sendMessage("Nie znaleziono strony $page", CommandSender.MessageSeverity.WARN)
            return
        }

        sender.sendMessage("Lista dostępnych komend ($page/${paged.maxPage}): ")

        for (currentCommand in paged.getValuesForPage(page))
        {
            sender.sendMessage(".$currentCommand")
        }

        if (paged.isThereANextPage(page))
        {
            sender.sendMessage("Aby zobaczyć kolejną strone użyj: .help ${page + 1}")
        }
    }
}