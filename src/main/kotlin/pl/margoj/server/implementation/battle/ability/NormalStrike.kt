package pl.margoj.server.implementation.battle.ability

import pl.margoj.server.implementation.battle.BattleImpl
import pl.margoj.server.implementation.battle.BattleData
import pl.margoj.server.implementation.battle.BattleLogBuilder
import pl.margoj.server.implementation.entity.EntityImpl
import pl.margoj.server.implementation.player.PlayerImpl

class NormalStrike(battle: BattleImpl, user: EntityImpl, target: EntityImpl) : BattleAbility(battle, user, target)
{
    override fun check(userData: BattleData, targetData: BattleData): Boolean
    {
        if (userData.team == targetData.team)
        {
            return false
        }

        if (!userData.canReach(targetData.row))
        {
            (user as? PlayerImpl)?.displayAlert("Jesteś zbyt daleko by zaatakować!")
            return false
        }

        return true
    }

    override fun onUse(userData: BattleData, targetData: BattleData)
    {
        val damage = 100
        target.damage(damage)
        targetData.updatedNow()

        val log = BattleLogBuilder()
        log.damager = userData
        log.damaged = targetData
        log.damage = damage
        log.damageTaken = damage

        battle.addLog(log.toString())
    }
}