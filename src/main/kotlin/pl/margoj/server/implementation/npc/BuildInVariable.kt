package pl.margoj.server.implementation.npc

import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext

abstract class BuildInVariable
{
    abstract fun getValue(context: ScriptContext, variableName: String): Any
}