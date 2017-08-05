package pl.margoj.server.implementation.npc.parser.buildin

import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext

abstract class BuildInVariable
{
    abstract fun getValue(context: ScriptContext, variableName: String): Any
}