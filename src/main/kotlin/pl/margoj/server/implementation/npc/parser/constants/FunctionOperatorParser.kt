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
            val operatorString: String
                get() = " " + this.name + (if (this.argumentsCount != 0) " " else "")

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
            ExpressionConstantParser.OPERATOR_ORDER[3] = this.functionData.map { it.operatorString }
        }

        init
        {
            this.registerFunction("posiada", 1)
            this.registerFunction("nie posiada", 1)
            this.registerFunction("dodaj", 1)
            this.registerFunction("zabierz", 1)
            this.registerFunction("dodaj złoto", 1)
            this.registerFunction("zabierz złoto", 1)
            this.registerFunction("ustaw hp", 1)
            this.registerFunction("rozpocznij walkę", 0)
            this.registerFunction("teleportuj na mape", 1)
            this.registerFunction("teleportuj na koordynaty", 2)
            this.registerFunction("teleportuj do", 3)
            this.registerFunction("zabij", 0)
            this.recalculateOperatorOrder()
        }
    }

    override fun canParse(operator: String): Boolean
    {
        return functionData.any { operator == it.operatorString }
    }

    override fun parse(parser: CodeParser, line: String, operatorStartPosition: Int, operatorEndPosition: Int, operator: String): Expression
    {
        val target = line.substring(0, operatorStartPosition - 1).trim()
        val parameters = line.substring(operatorEndPosition + 1).trim()
        val realIndex = parser.currentLine!!.realIndex
        val codeLine = CodeLine(realIndex, parameters)

        val functionData = functionData.find { it.name == operator.trim() } ?: parser.throwError("nieznana funkcja $operator")
        val parametersArray = Array(functionData.argumentsCount, { codeLine.skipSpaces(); parser.parseLiteral(codeLine) ?: parser.throwError("zbyt malo argumentow") })

        return FunctionExpression(operator.trim(), parser.parseLiteral(CodeLine(realIndex, target))!!, parametersArray)
    }
}