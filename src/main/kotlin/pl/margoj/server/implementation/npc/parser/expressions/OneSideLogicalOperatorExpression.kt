package pl.margoj.server.implementation.npc.parser.expressions

import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext

class
OneSideLogicalOperatorExpression(val rightSide: Any, val operator: String) : Expression()
{
    override fun execute(context: ScriptContext): Any
    {
        val right = asNumber(context.fetch(this.rightSide))

        return when (operator)
        {
            "nie " ->
            {
                if (asBoolean(right)) 0 else 1
            }
            else -> throw IllegalArgumentException("operator: $operator")
        }
    }
}