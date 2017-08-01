package pl.margoj.server.implementation.npc.parser.statements

import pl.margoj.server.implementation.npc.parser.CodeLine
import pl.margoj.server.implementation.npc.parser.CodeParser
import pl.margoj.server.implementation.npc.parser.blocks.CodeBlock
import pl.margoj.server.implementation.npc.parser.expressions.Expression
import pl.margoj.server.implementation.npc.parser.parsed.NpcCodeBlock
import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext

class IfStatement(val name: String, val parser: CodeParser, val line: CodeLine) : CodeStatement()
{
    private companion object
    {
        var ifLabelCounter = 1
    }

    private val expression: Any = parser.parseLiteral()!!
    private val codeBlock: CodeBlock = CodeBlock("IF_LABEL_${ifLabelCounter++}")
    private val npcCodeBlock: NpcCodeBlock by lazy { NpcCodeBlock(this.codeBlock) }

    init
    {
        parser.openBlock(codeBlock)
    }

    override fun execute(context: ScriptContext)
    {
        if(Expression.evaluate(context, this.expression))
        {
            npcCodeBlock.execute(context)
        }
    }
}