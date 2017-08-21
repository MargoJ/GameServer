package pl.margoj.server.implementation.npc.parser.fetchers

import pl.margoj.server.implementation.npc.parser.ValueFetcher
import pl.margoj.server.implementation.npc.parser.expressions.Expression
import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext

class ExpressionValueFetcher : ValueFetcher<Expression>()
{
    override val type: Class<Expression> = Expression::class.java

    override fun fetch(context: ScriptContext, value: Expression): Any?
    {
        return value.execute(context)
    }
}