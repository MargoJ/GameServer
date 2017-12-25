package pl.margoj.server.implementation.entity

import pl.margoj.server.api.battle.BattleUnableToStartException
import pl.margoj.server.api.battle.DamageSource
import pl.margoj.server.api.entity.LivingEntity
import pl.margoj.utils.commons.numbers.fastPow2
import pl.margoj.server.implementation.battle.BattleData
import pl.margoj.server.implementation.battle.BattleImpl

abstract class LivingEntityImpl : EntityImpl(), LivingEntity
{
    override var currentBattle: BattleImpl? = null

    override val isDead: Boolean get() = if (deadUntil == null) false else (deadUntil!!.time > System.currentTimeMillis())

    open var battleData: BattleData? = null

    override val canAnnounce: Boolean get() = !this.isDead

    val inActiveBattle: Boolean
        get()
        {
            return this.currentBattle != null && !this.currentBattle!!.finished && this.battleData?.dead == false
        }

    open val battleUnavailabilityCause: BattleUnableToStartException.Cause?
        get()
        {
            return when
            {
                this.isDead -> BattleUnableToStartException.Cause.ENTITY_IS_DEAD
                this.inActiveBattle -> BattleUnableToStartException.Cause.ENTITY_IN_BATTLE
                else -> null
            }
        }

    override val healthPercent: Int
        get()
        {
            return Math.ceil((this.hp.toDouble() / this.stats.maxHp.toDouble()) * 100.0).toInt()
        }

    override val damageSourceName: String
        get() = "${this.name}(${this.level}${this.stats.profession.id})"

    abstract val withGroup: List<LivingEntityImpl>

    open var lastDamageSource: DamageSource? = null

    override fun damage(amount: Int, damageSource: DamageSource?)
    {
        this.lastDamageSource = damageSource
        this.hp -= amount
        this.hp = Math.max(0, this.hp)
        if (this.hp == 0)
        {
            this.kill(damageSource)
        }
    }

    override fun kill(damageSource: DamageSource?)
    {
        this.lastDamageSource = damageSource
        this.battleData?.dead = true
    }

    open val killTime: Long
        get()
        {
            if (this.level >= 200)
            {
                return 18L * 60_000L
            }
            var minutes = (0.7 + (0.18 * this.level) - (0.00045 * this.level.fastPow2()))
            if (minutes > 18.0)
            {
                minutes = 18.0
            }
            return (minutes * 60_000.0).toLong()
        }

    override fun destroy()
    {
        this.kill(null)
    }
}