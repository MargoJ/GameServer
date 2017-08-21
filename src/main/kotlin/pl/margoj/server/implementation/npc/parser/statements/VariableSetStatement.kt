package pl.margoj.server.implementation.npc.parser.statements

import pl.margoj.server.implementation.npc.parser.CodeLine
import pl.margoj.server.implementation.npc.parser.CodeParser
import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext

class VariableSetStatement : CodeStatement()
{
    companion object
    {
        const val VARIABLE_LETTER_REGEX_STRING = "[\\p{L}\\d.]"
        val VARIABLE_LETTER_REGEX = VARIABLE_LETTER_REGEX_STRING.toRegex()
    }

    private lateinit var variableName: String
    private lateinit var value: Any

    override fun init(function: String, parser: CodeParser, line: CodeLine)
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

        val rightSide = parser.parseLiteral() ?: parser.throwError("błąd składni: niepoprawna wartość")

        this.value = rightSide
    }

    override fun execute(context: ScriptContext)
    {
        context.setVariable(this.variableName, context.fetch(this.value))
    }
}