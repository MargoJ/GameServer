package pl.margoj.server.implementation.npc.parser.blocks

import pl.margoj.server.implementation.npc.parser.AbstractBlock
import pl.margoj.server.implementation.npc.parser.CodeLine
import pl.margoj.server.implementation.npc.parser.CodeParser

class GlobalBlock: AbstractBlock()
{
    companion object
    {
        val OBJECT_TYPES = HashMap<String, () -> AbstractBlock>(2)

        init
        {
            OBJECT_TYPES.put("npc", ::NpcBlock)
        }
    }

    override fun parse(codeParser: CodeParser, currentLine: CodeLine)
    {
        currentLine.skipSpaces()

        val blockType = currentLine.readUntilSpace()
        val supplier = OBJECT_TYPES[blockType] ?: codeParser.throwError("nieznana deklaracja: $blockType")
        val block = supplier()
        codeParser.openBlock(block)
    }
}