package pl.margoj.server.implementation.entity

import pl.margoj.server.api.entity.Entity
import pl.margoj.server.implementation.battle.Battle
import pl.margoj.server.implementation.battle.BattleData

abstract class EntityImpl(override val id: Int) : Entity
{
    var currentBattle: Battle? = null

    var battleData: BattleData? = null

    override val healthPercent: Int
        get()
        {
            return Math.ceil((this.hp.toDouble() / this.stats.maxHp.toDouble()) * 100.0).toInt()
        }

    override fun damage(amount: Int)
    {
        this.hp -= amount
        this.hp = Math.max(0, this.hp)
    }
}