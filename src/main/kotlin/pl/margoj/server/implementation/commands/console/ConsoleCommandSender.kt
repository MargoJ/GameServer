package pl.margoj.server.implementation.commands.console

import org.apache.logging.log4j.Level
import pl.margoj.server.api.Server
import pl.margoj.server.api.commands.CommandSender
import pl.margoj.server.api.commands.ConsoleCommandSender

class ConsoleCommandSenderImpl(override val server: Server) : ConsoleCommandSender
{
    override val name: String = "CONSOLE"

    override fun sendMessage(message: String, messageSeverity: CommandSender.MessageSeverity)
    {
        val level = when (messageSeverity)
        {
            CommandSender.MessageSeverity.LOG -> Level.INFO
            CommandSender.MessageSeverity.WARN -> Level.WARN
            CommandSender.MessageSeverity.ERROR -> Level.ERROR
        }

        server.logger.log(level, "[COMMAND] $message")
    }
}