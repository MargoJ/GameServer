package pl.margoj.server.implementation.npc.parser.buildin

import pl.margoj.server.implementation.item.ItemImpl
import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext

class ItemBuildInVariable(val item: ItemImpl) : BuildInVariable()
{
    override fun getValue(context: ScriptContext, variableName: String): Any
    {
        return when (variableName)
        {
            "id" -> item.id
            "nazwa" -> item.name
            else -> "???"
        }
    }
}