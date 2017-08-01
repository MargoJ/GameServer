package pl.margoj.server.implementation.npc.parser.parsed

import pl.margoj.server.api.player.Player
import pl.margoj.server.implementation.npc.BuildInVariable
import pl.margoj.server.implementation.npc.parser.ValueFetcher

data class ScriptContext(val player: Player)
{
    private var variables: MutableMap<String, Any> = LinkedHashMap()
    var delegate: ((String, Array<Any>, ScriptContext) -> Unit)? = null

    fun fetch(parameter: Any): Any
    {
        var current = parameter

        for (fetcher in ValueFetcher.ALL)
        {
            if (fetcher.type.isInstance(current))
            {
                @Suppress("UNCHECKED_CAST")
                fetcher as ValueFetcher<Any>
                val returned = fetcher.fetch(this, current)
                if (returned != null)
                {
                    current = returned
                }
            }
        }

        return current
    }

    fun getVariable(name: String): Any?
    {
        if (!name.contains("."))
        {
            val value = this.variables[name] ?: return null
            return this.fetch(value)
        }
        else
        {
            val dot = name.indexOf('.')
            val buildInVariable = this.variables[name.substring(0, dot)] as? BuildInVariable ?: return "!INVALID_OBJECT"

            return buildInVariable.getValue(this, name.substring(dot + 1))
        }
    }

    fun setVariable(name: String, value: Any)
    {
        this.variables.put(name, value)
    }
}