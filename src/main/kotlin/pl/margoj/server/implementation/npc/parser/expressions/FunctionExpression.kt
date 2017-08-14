package pl.margoj.server.implementation.npc.parser.expressions

import pl.margoj.server.implementation.npc.parser.buildin.BuildInVariable
import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext

class FunctionExpression(val functionName: String, val target: Any, val parameters: Array<Any>) : Expression()
{
    override fun execute(context: ScriptContext): Any
    {
        val value = context.fetch(this.target) as? BuildInVariable ?: throw IllegalArgumentException("value not an object")

        return value.execute(context, this.functionName, parameters.map { context.fetch(it); }.toTypedArray())
    }
}