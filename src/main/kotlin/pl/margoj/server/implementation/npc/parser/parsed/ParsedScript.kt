package pl.margoj.server.implementation.npc.parser.parsed

import pl.margoj.server.implementation.npc.parser.blocks.GlobalBlock
import pl.margoj.server.implementation.npc.parser.blocks.NpcBlock

class ParsedScript(global: GlobalBlock)
{
    private val npcScripts = hashMapOf<String, NpcParsedScript>()
    val allScripts: Collection<NpcParsedScript> get() = this.npcScripts.values

    init
    {
        for (child in global.children)
        {
            if(child is NpcBlock)
            {
                npcScripts.put(child.name, NpcParsedScript(child))
            }
        }
    }

    fun getNpcScript(name: String): NpcParsedScript?
    {
        return this.npcScripts[name]
    }
}