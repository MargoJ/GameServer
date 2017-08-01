package pl.margoj.server.implementation.npc.parser.fetchers

import pl.margoj.server.implementation.npc.parser.ValueFetcher
import pl.margoj.server.implementation.npc.parser.constants.VariableConstant
import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext

class VariableValueFetcher : ValueFetcher<VariableConstant>()
{
    override val type: Class<VariableConstant> = VariableConstant::class.java

    override fun fetch(context: ScriptContext, value: VariableConstant): Any?
    {
        return context.getVariable(value.name)
    }
}