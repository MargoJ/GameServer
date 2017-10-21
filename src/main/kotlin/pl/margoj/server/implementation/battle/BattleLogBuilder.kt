package pl.margoj.server.implementation.battle

import pl.margoj.server.implementation.entity.EntityImpl
import java.util.stream.Collectors

class BattleLogBuilder
{
    var who: BattleData? = null
    var target: BattleData? = null

    var damager: BattleData? = null
    var damaged: BattleData? = null

    var damage: Int? = null
    var damageTaken: Int? = null

    var damageBlocked: Int? = null
    var damageEvaded = false
    var footshot = false

    var expGained: Long? = null

    var text: String? = null
    var step: Boolean = false
    var winner: List<EntityImpl>? = null

    override fun toString(): String
    {
        val builder = StringBuilder()

        if (who != null)
        {
            builder.append(who!!.id).append(";")
        }
        else if (damager != null)
        {
            builder.append(damager!!.id).append("=").append(damager!!.entity.healthPercent).append(";")
        }
        else
        {
            builder.append("0;")
        }

        if (target != null)
        {
            builder.append(target!!.id).append(";")
        }
        else if (damaged != null)
        {
            builder.append(damaged!!.id).append("=").append(damaged!!.entity.healthPercent).append(";")
        }
        else
        {
            builder.append("0;")
        }

        fun <T> appendIfNotNull(any: T?, name: String, toString: (T) -> String = { it.toString() })
        {
            if (any == null)
            {
                return
            }

            builder.append(name).append("=").append(toString(any)).append(";")
        }

        fun appendIfTrue(any: Boolean, name: String)
        {
            if (any)
            {
                builder.append(name).append(";")
            }
        }

        appendIfNotNull(text, "txt")

        appendIfNotNull(damage, "+dmg")
        appendIfNotNull(damageTaken, "-dmg")

        appendIfNotNull(damageBlocked, "-blok")
        appendIfTrue(damageEvaded, "-evade")
        appendIfTrue(footshot, "footshoot")

        appendIfNotNull(expGained, "+exp")
        appendIfNotNull(winner, "winner", { if (it.isEmpty()) "?" else it.stream().map { it.name }.collect(Collectors.joining(", ")) })
        appendIfTrue(step, "step")

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