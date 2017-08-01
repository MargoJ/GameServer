package pl.margoj.server.implementation.npc.parser.statements

import pl.margoj.server.implementation.npc.parser.CodeLine
import pl.margoj.server.implementation.npc.parser.CodeParser
import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext

class VariableSetStatement(val name: String, val parser: CodeParser, val line: CodeLine) : CodeStatement()
{
    companion object
    {
        const val VARIABLE_LETTER_REGEX_STRING = "[\\p{L}\\d.]"
        val VARIABLE_LETTER_REGEX = VARIABLE_LETTER_REGEX_STRING.toRegex()
    }

    private var variableName: String
    private var value: Any

    init
    {
        line.skipSpaces()

        val leftSide = line.readUntil('=').trim()
        if (!leftSide.matches("!$VARIABLE_LETTER_REGEX_STRING+".toRegex()))
        {
            parser.throwError("błąd składni: niepoprawna nazwa zmiennej")
        }

        this.variableName = leftSide.substring(1)

        line.next() // =

        line.skipSpaces()

        val rightSide = parser.parseLiteral()
        if (rightSide == null)
        {
            parser.throwError("błąd składni: niepoprawna wartość")
        }

        this.value = rightSide
    }

    override fun execute(context: ScriptContext)
    {
        context.setVariable(this.variableName, context.fetch(this.value))
    }
}