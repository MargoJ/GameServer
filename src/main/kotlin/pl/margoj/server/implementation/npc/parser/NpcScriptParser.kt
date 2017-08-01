package pl.margoj.server.implementation.npc.parser

import pl.margoj.server.implementation.npc.parser.blocks.GlobalBlock
import pl.margoj.server.implementation.npc.parser.parsed.ParsedScript

class NpcScriptParser
{
    private val scripts: MutableMap<String, ParsedScript> = hashMapOf()
    val allScripts: Collection<ParsedScript> get() = this.scripts.values

    fun parse(id: String, code: String): ParsedScript
    {
        val context = CodeParser(code)

        while (context.hasMore())
        {
            context.parseLine()
        }

        val global = context.finish() as GlobalBlock

        val parsedScript = ParsedScript(global)
        this.scripts.put(id, parsedScript)
        return parsedScript
    }

    fun getScript(id: String): ParsedScript?
    {
        return this.scripts[id]
    }
}