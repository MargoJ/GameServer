package pl.margoj.server.implementation.npc.parser.statements

import pl.margoj.server.implementation.npc.parser.CodeLine
import pl.margoj.server.implementation.npc.parser.CodeParser
import pl.margoj.server.implementation.npc.parser.constants.VariableConstant
import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext

class MathStatement(val name: String, val parser: CodeParser, val line: CodeLine) : CodeStatement()
{
    private val variable: VariableConstant
    private val number: Any

    init
    {
        line.skipSpaces()
        variable = parser.parseLiteral() as VariableConstant

        line.skipSpaces()
        number = parser.parseLiteral()!!
    }

    override fun execute(context: ScriptContext)
    {
        val current = context.getVariable(this.variable.name) as Long
        val number = context.fetch(this.number) as Long

        context.setVariable(variable.name, when (name)
        {
            ADD -> current + number
            SUBTRACT -> current - number
            MULTIPLY -> current * number
            DIVIDE -> current / number
            else -> "NaN"
        })
    }

    companion object
    {
        const val ADD = "dodaj"
        const val SUBTRACT = "odejmij"
        const val MULTIPLY = "pomnóż"
        const val DIVIDE = "podziel"
    }
}