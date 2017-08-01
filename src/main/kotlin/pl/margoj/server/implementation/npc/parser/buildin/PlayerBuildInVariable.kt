package pl.margoj.server.implementation.npc.parser.buildin

import pl.margoj.server.implementation.npc.BuildInVariable
import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext
import pl.margoj.server.implementation.player.PlayerImpl

class PlayerBuildInVariable(val player: PlayerImpl) : BuildInVariable()
{
    override fun getValue(context: ScriptContext, property: String): Any
    {
        return when (property)
        {
            "nick" -> player.name
            "level", "poziom" -> player.data.level
            else -> "???"
        }
    }
}