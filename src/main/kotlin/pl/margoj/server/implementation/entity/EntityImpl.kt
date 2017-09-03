package pl.margoj.server.implementation.entity

import pl.margoj.server.api.battle.BattleUnableToStartException
import pl.margoj.server.api.entity.Entity
import pl.margoj.server.api.player.Player
import pl.margoj.server.implementation.battle.BattleImpl
import pl.margoj.server.implementation.battle.BattleData

abstract class EntityImpl(override val id: Int) : Entity
{
    override var currentBattle: BattleImpl? = null

    override val isDead: Boolean get() = if (deadUntil == null) false else (deadUntil!!.time > System.currentTimeMillis())

    var battleData: BattleData? = null

    open val battleUnavailabilityCause: BattleUnableToStartException.Cause?
        get()
        {
            return when
            {
                this.isDead ->  BattleUnableToStartException.Cause.ENTITY_IS_DEAD
                this.currentBattle != null && !this.currentBattle!!.finished && this.battleData?.dead == false -> BattleUnableToStartException.Cause.ENTITY_IN_BATTLE
                else -> null
            }
        }

    override val healthPercent: Int
        get()
        {
            return Math.ceil((this.hp.toDouble() / this.stats.maxHp.toDouble()) * 100.0).toInt()
        }

    abstract val withGroup: List<EntityImpl>

    override fun damage(amount: Int)
    {
        this.hp -= amount
        this.hp = Math.max(0, this.hp)
    }
}