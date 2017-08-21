package pl.margoj.server.implementation.commands.defaults

import org.apache.commons.lang3.StringUtils
import pl.margoj.server.api.commands.Arguments
import pl.margoj.server.api.commands.CommandListener
import pl.margoj.server.api.commands.CommandSender
import java.util.stream.Collectors

class TownsCommand : CommandListener
{
    override fun commandPerformed(command: String, sender: CommandSender, args: Arguments)
    {
        var stream = sender.server.towns.stream()

        if (args.has(0))
        {
            val pattern = args.asString(0)
            stream = stream.filter { StringUtils.containsIgnoreCase(it.id, pattern) || StringUtils.containsIgnoreCase(it.name, pattern) }
        }

        val out = stream.map { " - ${it.id} [${it.name}] \n" }.collect(Collectors.joining())
        sender.sendMessage("Dostępne mapy: \n$out")
    }
}
