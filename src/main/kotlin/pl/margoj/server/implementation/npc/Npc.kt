package pl.margoj.server.implementation.npc

import pl.margoj.server.api.map.ImmutableLocation
import pl.margoj.server.implementation.entity.EntityImpl
import pl.margoj.server.implementation.npc.parser.parsed.NpcParsedScript
import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext
import java.util.concurrent.atomic.AtomicInteger

class Npc(val script: NpcParsedScript?, override val location: ImmutableLocation, val type: NpcType) : EntityImpl(npcIdCounter.incrementAndGet())
{
    override var name: String = ""
    override val direction: Int = 0
    var graphics: String = ""
    var level: Int = 1

    fun loadData()
    {
        val dataBlock = script?.getNpcCodeBlock("dane") ?: return
        val context = ScriptContext(null, this)
        context.delegate = this::delegateData
        dataBlock.execute(context)
    }

    private fun delegateData(function: String, parameters: Array<Any>, context: ScriptContext)
    {
        when (function)
        {
            "grafika" -> this.graphics = parameters[0] as String
            "nazwa" -> this.name = parameters[0] as String
            "poziom", "level" -> this.level = (parameters[0] as Long).toInt()
        }
    }

    companion object
    {
        private val npcIdCounter = AtomicInteger(2_000_000)
    }
}