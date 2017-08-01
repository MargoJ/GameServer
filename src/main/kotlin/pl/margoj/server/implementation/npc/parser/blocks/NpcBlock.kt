package pl.margoj.server.implementation.npc.parser.blocks

import pl.margoj.server.implementation.npc.parser.AbstractBlock
import pl.margoj.server.implementation.npc.parser.CodeLine
import pl.margoj.server.implementation.npc.parser.CodeParser

class NpcBlock : AbstractBlock()
{
    override lateinit var name: String

    override fun start()
    {
        this.line.skipSpaces()
        this.name = this.parser.parseLiteral() as? String ?: parser.throwError("nazwa NPC musi być stringiem")
    }

    override fun parse(codeParser: CodeParser, currentLine: CodeLine)
    {
        val label = currentLine.readUntilSpace()
        if (!label.startsWith("@") && label.length > 1)
        {
            codeParser.throwError("spodziewano się: @label")
        }

        val labelName = label.substring(1)
        codeParser.openBlock(CodeBlock(labelName))
    }
}