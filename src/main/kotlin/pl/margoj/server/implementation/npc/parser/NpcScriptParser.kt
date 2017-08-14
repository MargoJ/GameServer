package pl.margoj.server.implementation.npc.parser

import pl.margoj.server.implementation.npc.parser.blocks.GlobalBlock
import pl.margoj.server.implementation.npc.parser.constants.FunctionOperatorParser
import pl.margoj.server.implementation.npc.parser.parsed.NpcParsedScript
import pl.margoj.server.implementation.npc.parser.parsed.ParsedScript

class NpcScriptParser
{
    private val scripts: MutableMap<String, ParsedScript> = hashMapOf()
    private val npcScripts: MutableMap<String, NpcParsedScript> = hashMapOf()
    val allScripts: Collection<ParsedScript> get() = this.scripts.values

    fun parse(id: String, code: String): ParsedScript
    {
        val context = CodeParser(code)

        // init to populate operator list
        FunctionOperatorParser()

        while (context.hasMore())
        {
            context.parseLine()
        }

        val global = context.finish() as GlobalBlock

        val parsedScript = ParsedScript(global)
        for (script in parsedScript.allScripts)
        {
            this.npcScripts.putIfAbsent(script.name, script)
        }

        this.scripts.put(id, parsedScript)
        return parsedScript
    }

    fun getScript(id: String): ParsedScript?
    {
        return this.scripts[id]
    }

    fun getNpcScript(id: String): NpcParsedScript?
    {
        return this.npcScripts[id]
    }
}