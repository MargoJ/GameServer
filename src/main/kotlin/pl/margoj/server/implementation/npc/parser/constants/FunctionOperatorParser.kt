package pl.margoj.server.implementation.npc.parser.constants

import pl.margoj.server.implementation.npc.parser.CodeLine
import pl.margoj.server.implementation.npc.parser.CodeParser
import pl.margoj.server.implementation.npc.parser.expressions.Expression
import pl.margoj.server.implementation.npc.parser.expressions.FunctionExpression
import pl.margoj.server.implementation.npc.parser.expressions.OperatorParser
import java.util.TreeSet

class FunctionOperatorParser : OperatorParser()
{
    companion object
    {
        private val functionData = TreeSet<FunctionData>()

        private data class FunctionData
        (
                val name: String,
                val argumentsCount: Int
        ) : Comparable<FunctionData>
        {
            override fun compareTo(other: FunctionData): Int
            {
                val diff = other.name.count { it == ' ' } - this.name.count { it == ' ' }
                if (diff == 0)
                {
                    return this.name.compareTo(other.name)
                }
                return diff
            }
        }

        fun registerFunction(name: String, argumentsCount: Int)
        {
            this.functionData.add(FunctionData(name, argumentsCount))
        }

        fun recalculateOperatorOrder()
        {
            ExpressionConstantParser.OPERATOR_ORDER[3] = this.functionData.map { " ${it.name} " }
        }

        init
        {
            this.registerFunction("posiada", 1);
            this.registerFunction("nie posiada", 1);
            this.registerFunction("dodaj", 1);
            this.registerFunction("zabierz", 1);
            this.registerFunction("dodaj złoto", 1);
            this.registerFunction("zabierz złoto", 1);

            this.recalculateOperatorOrder()
        }
    }

    override fun canParse(operator: String): Boolean
    {
        return functionData.any { operator == " ${it.name} " }
    }

    override fun parse(parser: CodeParser, line: String, operatorStartPosition: Int, operatorEndPosition: Int, operator: String): Expression
    {
        val target = line.substring(0, operatorStartPosition - 1).trim()
        val parameters = line.substring(operatorEndPosition + 1).trim()
        val realIndex = parser.currentLine!!.realIndex

        val functionData = functionData.find { it.name == operator.trim() } ?: parser.throwError("nieznana funkcja $operator")
        val parametersArray = Array(functionData.argumentsCount, { parser.parseLiteral(CodeLine(realIndex, parameters)) ?: parser.throwError("zbyt malo argumentow") })

        return FunctionExpression(operator.trim(), parser.parseLiteral(CodeLine(realIndex, target))!!, parametersArray)
    }
}