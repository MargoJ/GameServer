package pl.margoj.server.implementation.commands

import pl.margoj.server.api.commands.Arguments
import pl.margoj.server.api.commands.CommandException
import pl.margoj.server.api.player.Player
import pl.margoj.server.api.utils.Parse
import pl.margoj.server.implementation.ServerImpl

class StringArrayArguments(val server: ServerImpl, val array: Array<String>) : Arguments
{
    override fun has(i: Int): Boolean
    {
        return i < array.size;
    }

    override fun asString(i: Int): String
    {
        this.ensureHas(i)
        return this.array[i]
    }

    override fun asInt(i: Int): Int?
    {
        return Parse.parseInt(this.asString(i))
    }

    override fun asLong(i: Int): Long?
    {
        return Parse.parseLong(this.asString(i))
    }

    override fun asPlayer(i: Int): Player?
    {
        val string = this.asString(i)
        return this.server.players.filter { it.name == string }.firstOrNull()
    }

    override fun ensureTrue(condition: () -> Boolean)
    {
        this.ensureTrue(condition, "Niepoprawne argumenty!")
    }

    override fun ensureTrue(condition: () -> Boolean, message: String)
    {
        if (!condition())
        {
            throw CommandException(message)
        }
    }

    override fun ensureNotNull(value: Any?)
    {
        this.ensureNotNull(value, "Niepoprawne argumenty!")
    }

    override fun ensureNotNull(value: Any?, message: String)
    {
        if (value == null)
        {
            throw CommandException(message)
        }
    }

    private fun ensureHas(i: Int)
    {
        this.ensureTrue({ this.has(i) }, "Zbyt mało argumentów")
    }
}