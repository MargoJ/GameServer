package pl.margoj.server.implementation.npc.parser.statements

import pl.margoj.server.implementation.npc.parser.CodeLine
import pl.margoj.server.implementation.npc.parser.CodeParser
import pl.margoj.server.implementation.npc.parser.blocks.CodeBlock
import pl.margoj.server.implementation.npc.parser.expressions.Expression
import pl.margoj.server.implementation.npc.parser.parsed.NpcCodeBlock
import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext

open class IfStatement : CodeStatement()
{
    companion object
    {
        var ifLabelCounter = 1
    }

    private lateinit var expression: Any
    val codeBlock: CodeBlock = CodeBlock("IF_LABEL_${ifLabelCounter++}")
    protected val npcCodeBlock: NpcCodeBlock by lazy { NpcCodeBlock(this.codeBlock) }
    var nextIf: IfStatement? = null

    override fun init(function: String, parser: CodeParser, line: CodeLine)
    {
        line.skipSpaces()
        this.expression = parser.parseLiteral()!!
        parser.openBlock(codeBlock)
    }

    open fun evaluateExpression(context: ScriptContext): Boolean
    {
        return Expression.evaluate(context, this.expression)
    }

    override fun execute(context: ScriptContext)
    {
        this.executeChained(context)
    }

    fun executeChained(context: ScriptContext)
    {
        if (this.evaluateExpression(context))
        {
            npcCodeBlock.execute(context)
        }
        else
        {
            this.nextIf?.executeChained(context)
        }
    }
}