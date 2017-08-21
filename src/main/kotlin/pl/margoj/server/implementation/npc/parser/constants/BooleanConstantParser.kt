package pl.margoj.server.implementation.npc.parser.constants

import pl.margoj.server.implementation.npc.parser.CodeLine
import pl.margoj.server.implementation.npc.parser.CodeParser
import pl.margoj.server.implementation.npc.parser.ConstantParser
import pl.margoj.server.implementation.npc.parser.statements.VariableSetStatement

class BooleanConstantParser : ConstantParser<Boolean>()
{
    companion object
    {
        val TRUE_VALUES = arrayOf("prawda")
        val FALSE_VALUES = arrayOf("fałsz")
    }

    override fun tryParse(parser: CodeParser, line: CodeLine): Boolean?
    {
        val word = line.readIfMatches(VariableSetStatement.VARIABLE_LETTER_REGEX)
        return when (word)
        {
            in TRUE_VALUES -> true
            in FALSE_VALUES -> false
            else -> null
        }
    }
}