package pl.margoj.server.implementation.npc.parser.constants

import pl.margoj.server.implementation.npc.parser.CodeLine
import pl.margoj.server.implementation.npc.parser.CodeParser
import pl.margoj.server.implementation.npc.parser.ConstantParser
import pl.margoj.server.implementation.npc.parser.statements.VariableSetStatement

data class VariableConstant(val name: String)

class VariableConstantParser : ConstantParser<VariableConstant>()
{
    override fun tryParse(parser: CodeParser, line: CodeLine): VariableConstant?
    {
        if (line.current != '!')
        {
            return null
        }
        line.next()

        val name = line.readIfMatches(VariableSetStatement.VARIABLE_LETTER_REGEX)

        return VariableConstant(name)
    }
}