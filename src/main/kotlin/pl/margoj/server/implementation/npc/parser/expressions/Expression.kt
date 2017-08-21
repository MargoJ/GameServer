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
            if (expression is Expression)
            {
                val expressionResult = context.fetch(expression.execute(context))

                return Expression.asBoolean(expressionResult)
            }
            else
            {
                return Expression.asBoolean(context.fetch(expression))
            }
        }

        fun asNumber(fetch: Any?): Long
        {
            when (fetch)
            {
                is Boolean -> return if (fetch) 1 else 0
                is String -> return fetch.length.toLong()
                is Number -> return fetch.toLong()
                else -> return if (fetch != null) 1 else 0
            }
        }

        fun asBoolean(fetch: Any?): Boolean
        {
            if (fetch is Boolean)
            {
                return fetch
            }
            if (fetch is Number)
            {
                return fetch.toLong() != 0L
            }
            if (fetch is String)
            {
                return fetch.isNotEmpty()
            }
            return fetch != null
        }
    }
}