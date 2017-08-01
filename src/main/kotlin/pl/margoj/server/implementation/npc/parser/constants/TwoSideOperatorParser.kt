package pl.margoj.server.implementation.npc.parser.constants

import pl.margoj.server.implementation.npc.parser.CodeLine
import pl.margoj.server.implementation.npc.parser.CodeParser
import pl.margoj.server.implementation.npc.parser.expressions.Expression
import pl.margoj.server.implementation.npc.parser.expressions.OperatorParser
import pl.margoj.server.implementation.npc.parser.expressions.TwoSideLogicalOperatorExpression

class TwoSideOperatorParser(vararg val operators: String) : OperatorParser()
{
    override fun canParse(operator: String): Boolean
    {
        return operator in operators
    }

    override fun parse(parser: CodeParser, line: String, operatorStartPosition: Int, operatorEndPosition: Int, operator: String): Expression
    {
        val leftSide = line.substring(0, operatorStartPosition - 1).trim()
        val rightSide = line.substring(operatorEndPosition + 1).trim()
        val realIndex = parser.currentLine!!.realIndex

        return TwoSideLogicalOperatorExpression(parser.parseLiteral(CodeLine(realIndex, leftSide))!!, parser.parseLiteral(CodeLine(realIndex, rightSide))!!, operator)
    }
}