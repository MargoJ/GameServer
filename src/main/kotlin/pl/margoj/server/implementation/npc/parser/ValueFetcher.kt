package pl.margoj.server.implementation.npc.parser

import pl.margoj.server.implementation.npc.parser.fetchers.ExpressionValueFetcher
import pl.margoj.server.implementation.npc.parser.fetchers.StringValueFetcher
import pl.margoj.server.implementation.npc.parser.fetchers.VariableValueFetcher
import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext

abstract class ValueFetcher<T>
{
    companion object
    {
        val ALL = ArrayList<ValueFetcher<*>>()

        init
        {
            ALL.add(VariableValueFetcher())
            ALL.add(StringValueFetcher())
            ALL.add(ExpressionValueFetcher())
        }
    }

    abstract val type: Class<T>

    abstract fun fetch(context: ScriptContext, value: T): Any?
}