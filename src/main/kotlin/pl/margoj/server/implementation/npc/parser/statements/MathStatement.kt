package pl.margoj.server.implementation.npc.parser.statements

import pl.margoj.server.implementation.npc.parser.CodeLine
import pl.margoj.server.implementation.npc.parser.CodeParser
import pl.margoj.server.implementation.npc.parser.constants.VariableConstant
import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext

class MathStatement : CodeStatement()
{
    private lateinit var name: String
    private lateinit var variable: VariableConstant
    private lateinit var number: Any

    override fun init(function: String, parser: CodeParser, line: CodeLine)
    {
        this.name = function

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