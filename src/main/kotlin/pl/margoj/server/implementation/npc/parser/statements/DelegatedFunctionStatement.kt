package pl.margoj.server.implementation.npc.parser.statements

import pl.margoj.server.implementation.npc.parser.CodeLine
import pl.margoj.server.implementation.npc.parser.CodeParser
import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext

class DelegatedFunctionStatement(val function: String, val parser: CodeParser, val line: CodeLine) : CodeStatement()
{
    private val parameters = ArrayList<Any>()

    init
    {
        while (true)
        {
            line.skipSpaces()

            if(line.finished)
            {
                break
            }

            parameters.add(parser.parseLiteral()!!)
        }
    }

    override fun execute(context: ScriptContext)
    {
        val parameters = Array(this.parameters.size, { context.fetch(parameters[it]) })

        context.delegate?.invoke(this.function, parameters, context)
    }
}