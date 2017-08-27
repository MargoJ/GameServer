package pl.margoj.server.implementation.battle

import pl.margoj.server.implementation.entity.EntityImpl
import java.util.stream.Collectors

class BattleLogBuilder
{
    var damager: BattleData? = null
    var damaged: BattleData? = null

    var damage: Int? = null
    var damageTaken: Int? = null

    var text: String? = null
    var winner: List<EntityImpl>? = null

    override fun toString(): String
    {
        val builder = StringBuilder()

        if (damager == null)
        {
            builder.append("0;")
        }
        else
        {
            builder.append(damager!!.id).append("=").append(damager!!.entity.healthPercent).append(";")
        }

        if (damaged == null)
        {
            builder.append("0;")
        }
        else
        {
            builder.append(damaged!!.id).append("=").append(damaged!!.entity.healthPercent).append(";")
        }

        fun <T> appendIfNotNull(any: T?, name: String, toString: (T) -> String = { it.toString() })
        {
            if (any == null)
            {
                return
            }

            builder.append(name).append("=").append(toString(any)).append(";")
        }

        appendIfNotNull(text, "txt")
        appendIfNotNull(damage, "+dmg")
        appendIfNotNull(damageTaken, "-dmg")
        appendIfNotNull(winner, "winner", { if (it.isEmpty()) "?" else it.stream().map { it.name }.collect(Collectors.joining(", ")) })

        if (builder.endsWith(";"))
        {
            builder.setLength(builder.length - 1)
        }

        return builder.toString()
    }

    inline fun build(function: (BattleLogBuilder) -> Unit): BattleLogBuilder
    {
        function(this)
        return this
    }
}