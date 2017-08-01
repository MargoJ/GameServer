package pl.margoj.server.implementation.npc.parser.expressions

import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext

abstract class Expression
{
    abstract fun execute(context: ScriptContext): Any

    protected fun throwIllegalType(): Nothing
    {
        throw IllegalArgumentException("illegal type")
    }


    companion object
    {
        fun evaluate(context: ScriptContext, expression: Any): Boolean
        {
            if(expression is Expression)
            {
                val expressionResult = expression.execute(context)

                return Expression.asBoolean(expressionResult)
            }
            else
            {
                return Expression.asBoolean(expression)
            }
        }

        fun asNumber(fetch: Any?): Long
        {
            if (fetch is Boolean)
            {
                return if (fetch) 1 else 0
            }
            else if (fetch is String)
            {
                return fetch.length.toLong()
            }
            else if (fetch is Number)
            {
                return fetch.toLong()
            }
            else
            {
                return if (fetch != null) 1 else 0
            }
        }

        fun asBoolean(fetch: Any?): Boolean
        {
            if(fetch is Boolean)
            {
                return fetch
            }
            if(fetch is Number)
            {
                return fetch.toLong() != 0L
            }
            if(fetch is String)
            {
                return fetch.isNotEmpty()
            }
            return fetch != null
        }
    }
}

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