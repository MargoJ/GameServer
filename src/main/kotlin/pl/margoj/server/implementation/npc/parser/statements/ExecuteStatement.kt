package pl.margoj.server.implementation.npc.parser.statements

import pl.margoj.server.implementation.npc.parser.CodeLine
import pl.margoj.server.implementation.npc.parser.CodeParser
import pl.margoj.server.implementation.npc.parser.Label
import pl.margoj.server.implementation.npc.parser.constants.ExpressionConstantParser
import pl.margoj.server.implementation.npc.parser.expressions.Expression
import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext

open class ExecuteStatement : CodeStatement()
{
    private lateinit var expression: Any

    override fun init(function: String, parser: CodeParser, line: CodeLine)
    {
        line.skipSpaces()
        val oldIndex = line.currentIndex
        this.expression = parser.parseLiteral()!!

        if (this.expression !is Label && this.expression !is Expression)
        {
            line.currentIndex = oldIndex
            this.expression = ExpressionConstantParser.INSTANCE.tryParse(parser, line)!!
        }
    }

    override fun execute(context: ScriptContext)
    {
        when
        {
            this.expression is Expression -> (this.expression as Expression).execute(context)
            this.expression is Label -> context.nextLabel!!.invoke(this.expression as Label, context)
            else -> throw IllegalStateException("invalid expression type: $expression")
        }
    }
}