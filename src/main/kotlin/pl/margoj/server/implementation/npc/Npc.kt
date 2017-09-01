package pl.margoj.server.implementation.npc

import pl.margoj.server.api.Server
import pl.margoj.server.api.map.ImmutableLocation
import pl.margoj.server.api.player.Gender
import pl.margoj.server.implementation.entity.EntityImpl
import pl.margoj.server.implementation.npc.parser.parsed.NpcParsedScript
import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext
import java.util.Date
import java.util.concurrent.atomic.AtomicInteger

class Npc(val script: NpcParsedScript?, override val location: ImmutableLocation, val type: NpcType, override val server: Server) : EntityImpl(npcIdCounter.incrementAndGet())
{
    override var name: String = ""
    override val direction: Int = 0
    override var icon: String = ""
    override val gender: Gender = Gender.UNKNOWN // TODO

    override var level: Int
        get() = this.stats.level
        set(value)
        {
            this.stats.level = value
        }

    override val stats = NpcData(this)
    override var hp: Int = 100

    override var deadUntil: Date? = null

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
            "grafika" -> this.icon = parameters[0] as String
            "nazwa" -> this.name = parameters[0] as String
            "poziom", "level" -> this.stats.level = (parameters[0] as Long).toInt()
        }
    }

    override fun toString(): String
    {
        return "Npc(id=$id, name=$name, location=$location)"
    }

    companion object
    {
        private val npcIdCounter = AtomicInteger(2_000_000)
    }
}