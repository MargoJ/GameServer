package pl.margoj.server.implementation.npc.parser.statements

import pl.margoj.server.implementation.npc.parser.CodeLine
import pl.margoj.server.implementation.npc.parser.CodeParser
import pl.margoj.server.implementation.npc.parser.blocks.CodeBlock
import pl.margoj.server.implementation.npc.parser.expressions.Expression
import pl.margoj.server.implementation.npc.parser.parsed.NpcCodeBlock
import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext

class WhileStatement : CodeStatement()
{
    private companion object
    {
        var whileLabelCounter = 1
    }

    private lateinit var expression: Any
    private val codeBlock: CodeBlock = CodeBlock("WHILE_LABEL_${whileLabelCounter++}")
    private val npcCodeBlock: NpcCodeBlock by lazy { NpcCodeBlock(this.codeBlock) }

    override fun init(function: String, parser: CodeParser, line: CodeLine)
    {
        line.skipSpaces()
        this.expression = parser.parseLiteral()!!
        parser.openBlock(codeBlock)
    }

    override fun execute(context: ScriptContext)
    {
        while (Expression.evaluate(context, this.expression))
        {
            npcCodeBlock.execute(context)
        }
    }
}