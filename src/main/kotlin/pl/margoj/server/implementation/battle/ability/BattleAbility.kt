package pl.margoj.server.implementation.battle.ability

import pl.margoj.server.implementation.battle.BattleImpl
import pl.margoj.server.implementation.battle.BattleData
import pl.margoj.server.implementation.entity.EntityImpl

abstract class BattleAbility(val battle: BattleImpl, val user: EntityImpl, val target: EntityImpl)
{
    fun queue()
    {
        if(!battle.isAbilityValid(this))
        {
            return
        }

        if(this.check(user.battleData!!, target.battleData!!))
        {
            battle.queueMove(this)
        }
    }

    internal fun performNow(): Boolean
    {
        val result = this.check(user.battleData!!, target.battleData!!)

        if (result)
        {
            this.onUse(user.battleData!!, target.battleData!!)
            battle.moveDone(this.user)
        }

        return result
    }

    protected abstract fun check(userData: BattleData, targetData: BattleData): Boolean

    protected abstract fun onUse(userData: BattleData, targetData: BattleData)
}