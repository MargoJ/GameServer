package pl.margoj.server.implementation.npc.parser.constants

import pl.margoj.server.implementation.npc.parser.CodeLine
import pl.margoj.server.implementation.npc.parser.CodeParser
import pl.margoj.server.implementation.npc.parser.ConstantParser
import pl.margoj.server.implementation.npc.parser.expressions.Expression
import pl.margoj.server.implementation.npc.parser.expressions.OperatorParser

class ExpressionConstantParser : ConstantParser<Expression>()
{
    companion object
    {
        val OPERATOR_ORDER = arrayOf(
                arrayOf("&&", "||", " i ", " oraz ", " lub "),
                arrayOf("nie "),
                arrayOf("==", "=", "!=", "<=", ">=", "<", ">")
        )
    }

    override fun tryParse(parser: CodeParser, line: CodeLine): Expression?
    {
        val first = line.currentIndex
        val whole = line.rest()
        var operator: String? = null
        var operatorStartIndex: Int? = null
        var operatorEndIndex: Int? = null

        main@
        for (operators in OPERATOR_ORDER)
        {
            line.currentIndex = first

            while (!line.finished)
            {
                StringConstantParser.INSTANCE.tryParse(parser, line)
                if (line.finished)
                {
                    break
                }

                for (currentOperator in operators)
                {
                    val operatorArray = currentOperator.toCharArray()

                    if (line.current == operatorArray[0])
                    {
                        operatorStartIndex = line.currentIndex

                        var found = true
                        for (i in 1..operatorArray.size - 1)
                        {
                            line.next()
                            if (line.finished)
                            {
                                found = false
                                break
                            }
                            if (line.current != operatorArray[i])
                            {
                                found = false
                            }
                        }

                        operatorEndIndex = line.currentIndex

                        if (found)
                        {
                            operator = currentOperator
                            break@main
                        }
                        else
                        {
                            line.currentIndex = operatorStartIndex
                        }
                    }
                }
                line.next()
            }
        }

        if (operator == null)
        {
            return null
        }

        operatorStartIndex!!
        operatorEndIndex!!
        operatorStartIndex -= first - 1
        operatorEndIndex -= first

        for (operatorParser in OperatorParser.ALL)
        {
            if (operatorParser.canParse(operator))
            {
                return operatorParser.parse(parser, whole, operatorStartIndex, operatorEndIndex, operator)
            }
        }

        return null
    }
}