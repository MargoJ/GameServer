package pl.margoj.server.implementation.battle.ability

import pl.margoj.server.implementation.battle.Battle
import pl.margoj.server.implementation.battle.BattleData
import pl.margoj.server.implementation.entity.EntityImpl

abstract class PlayerAbility(val battle: Battle, val user: EntityImpl)
{
    fun use(target: EntityImpl)
    {
        if (battle.finished || user.currentBattle != battle || target.currentBattle != battle)
        {
            return
        }
        if (user.battleData!!.dead || target.battleData!!.dead)
        {
            return
        }
        if (battle.currentEntity != user)
        {
            return
        }

        if (this.onUse(target, user.battleData!!, target.battleData!!))
        {
            battle.moveDone(user)
        }
    }

    abstract fun onUse(target: EntityImpl, userData: BattleData, targetData: BattleData): Boolean
}