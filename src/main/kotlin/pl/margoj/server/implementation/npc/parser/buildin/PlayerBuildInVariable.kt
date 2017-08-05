package pl.margoj.server.implementation.npc.parser.buildin

import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext
import pl.margoj.server.implementation.player.PlayerImpl

class PlayerBuildInVariable(val player: PlayerImpl) : BuildInVariable()
{
    override fun getValue(context: ScriptContext, variableName: String): Any
    {
        return when (variableName)
        {
            "nick" -> player.name
            "level", "poziom" -> player.data.level
            else -> "???"
        }
    }
}