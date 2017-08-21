package pl.margoj.server.implementation.npc.parser.parsed

import pl.margoj.server.implementation.npc.parser.blocks.CodeBlock
import pl.margoj.server.implementation.npc.parser.blocks.NpcBlock

class NpcParsedScript(block: NpcBlock)
{
    private var codeBlocks = hashMapOf<String, NpcCodeBlock>()
    val allScripts: Collection<NpcCodeBlock> get() = this.codeBlocks.values
    val name = block.name

    init
    {
        for (child in block.children)
        {
            if (child is CodeBlock)
            {
                codeBlocks.put(child.label, NpcCodeBlock(child))
            }
        }
    }

    fun getNpcCodeBlock(name: String): NpcCodeBlock?
    {
        return this.codeBlocks[name]
    }
}