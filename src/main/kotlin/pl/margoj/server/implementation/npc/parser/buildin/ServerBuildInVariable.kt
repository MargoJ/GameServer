package pl.margoj.server.implementation.npc.parser.buildin

import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext
import java.text.SimpleDateFormat
import java.util.Date

class ServerBuildInVariable(val server: ServerImpl) : BuildInVariable()
{
    override fun getValue(context: ScriptContext, variableName: String): Any
    {
        return when (variableName)
        {
            "nazwa" -> server.name
            "data" -> dateFormat.format(Date())
            "godzina" -> timeFormat.format(Date())
            "graczeOnline" -> server.players.map(::PlayerBuildInVariable)
            else -> "???"
        }
    }

    private companion object
    {
        private val dateFormat = SimpleDateFormat("dd-MM-yyyy")
        private val timeFormat = SimpleDateFormat("HH:mm:ss")
    }
}