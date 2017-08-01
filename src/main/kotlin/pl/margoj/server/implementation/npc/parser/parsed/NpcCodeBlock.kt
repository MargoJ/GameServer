package pl.margoj.server.implementation.npc.parser.parsed

import pl.margoj.server.implementation.npc.parser.blocks.CodeBlock

class NpcCodeBlock(val block: CodeBlock)
{
    fun execute(context: ScriptContext)
    {
        for (statement in block.statements)
        {
            statement.execute(context)
        }
    }
}
