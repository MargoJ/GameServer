package pl.margoj.server.implementation.commands

import org.apache.commons.lang3.StringUtils
import pl.margoj.server.api.commands.CommandException
import pl.margoj.server.api.commands.CommandListener
import pl.margoj.server.api.commands.CommandSender
import pl.margoj.server.api.commands.CommandsManager
import pl.margoj.server.api.plugin.MargoJPlugin
import pl.margoj.server.implementation.ServerImpl
import java.util.Arrays
import java.util.TreeSet

class CommandsManagerImpl(val server: ServerImpl) : CommandsManager
{
    private var allListeners = TreeSet<RegisteredListener>()

    override fun dispatchCommand(sender: CommandSender, string: String): Boolean
    {
        var input = string.trim()
        if (input.first() == '.' || input.first() == '/')
        {
            input = input.substring(1)
        }
        val parts = StringUtils.split(input, ' ')
        val command = parts[0]
        val args = parts.copyOfRange(1, parts.size)

        return commandDispatched(sender, command, args)
    }

    private fun commandDispatched(sender: CommandSender, command: String, args: Array<String>): Boolean
    {
        val listener = this.allListeners.filter { it.commands.contains(command) }.firstOrNull() ?: return false

        try
        {
            listener.listener.commandPerformed(command, sender, StringArrayArguments(args))

        }
        catch (e: CommandException)
        {
            sender.sendMessage(e.message!!, CommandSender.MessageSeverity.ERROR)
        }

        return true
    }

    override fun registerListener(plugin: MargoJPlugin<*>, listener: CommandListener, vararg commands: String)
    {
        this.allListeners.add(RegisteredListener(commands, listener, plugin))
    }

    fun registerCoreListener(listener: CommandListener, vararg commands: String)
    {
        this.allListeners.add(RegisteredListener(commands, listener, null))
    }

    override fun unregisterAll(owner: MargoJPlugin<*>): Boolean
    {
        return this.unregisterByCriteria { it.owner == owner }
    }

    override fun unregisterListener(listener: CommandListener): Boolean
    {
        return this.unregisterByCriteria { it.listener == listener }
    }

    private fun unregisterByCriteria(criteria: (RegisteredListener) -> Boolean): Boolean
    {
        var anyChanged = false
        val iterator = this.allListeners.iterator()

        while (iterator.hasNext())
        {
            if (criteria(iterator.next()))
            {
                iterator.remove()
                anyChanged = true
            }
        }

        return anyChanged
    }
}

data class RegisteredListener(
        var commands: Array<out String>,
        val listener: CommandListener,
        var owner: MargoJPlugin<*>?
) : Comparable<RegisteredListener>
{
    override fun compareTo(other: RegisteredListener): Int
    {
        return if (this.owner == null) 1 else -1
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as RegisteredListener

        if (!Arrays.equals(commands, other.commands)) return false
        if (listener != other.listener) return false
        if (owner != other.owner) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = Arrays.hashCode(commands)
        result = 31 * result + listener.hashCode()
        result = 31 * result + (owner?.hashCode() ?: 0)
        return result
    }


}