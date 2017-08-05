package pl.margoj.server.implementation.npc.parser.blocks

import pl.margoj.server.implementation.npc.parser.AbstractBlock
import pl.margoj.server.implementation.npc.parser.CodeLine
import pl.margoj.server.implementation.npc.parser.CodeParser
import pl.margoj.server.implementation.npc.parser.statements.CodeStatement
import java.util.LinkedList

class CodeBlock(val label: String) : AbstractBlock()
{
    override val name: String = label
    val statements = LinkedList<CodeStatement>()
    var lastStatement: CodeStatement? = null

    override fun start()
    {
    }

    override fun end()
    {
    }

    override fun parse(codeParser: CodeParser, currentLine: CodeLine)
    {
        this.lastStatement = codeParser.parseStatement()
        this.statements.add(this.lastStatement!!)
    }
}