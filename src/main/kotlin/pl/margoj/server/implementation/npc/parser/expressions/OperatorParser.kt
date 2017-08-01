package pl.margoj.server.implementation.npc.parser.expressions

import pl.margoj.server.implementation.npc.parser.CodeParser
import pl.margoj.server.implementation.npc.parser.constants.TwoSideOperatorParser

abstract class OperatorParser
{
    companion object
    {
        val ALL = ArrayList<OperatorParser>()

        init
        {
            ALL.add(TwoSideOperatorParser("&&", "||", " i ", " oraz ", " lub ", "=", "==", "!=", "<", ">", "<=", ">="))
        }
    }

    abstract fun canParse(operator: String): Boolean

    abstract fun parse(parser: CodeParser, line: String, operatorStartPosition: Int, operatorEndPosition: Int, operator: String): Expression
}