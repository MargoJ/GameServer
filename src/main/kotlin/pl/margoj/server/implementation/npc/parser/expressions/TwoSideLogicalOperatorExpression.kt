package pl.margoj.server.implementation.npc.parser.expressions

import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext

class TwoSideLogicalOperatorExpression(val leftSide: Any, val rightSide: Any, val operator: String) : Expression()
{
    override fun execute(context: ScriptContext): Any
    {
        val left = asNumber(context.fetch(this.leftSide))
        val right = asNumber(context.fetch(this.rightSide))

        return when (operator)
        {
            " i ", " oraz ", "&&" ->
            {
                left != 0L && right != 0L
            }
            " lub ", "||" ->
            {
                left != 0L || right != 0L
            }
            "=", "==" ->
            {
                left == right
            }
            "!=" ->
            {
                left != right
            }
            ">" ->
            {
                left > right
            }
            "<" ->
            {
                left < right
            }
            ">=" ->
            {
                left >= right
            }
            "<=" ->
            {
                left <= right
            }
            else -> throw IllegalArgumentException("operator: $operator")
        }
    }
}