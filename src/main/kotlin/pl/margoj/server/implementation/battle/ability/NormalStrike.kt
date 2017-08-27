package pl.margoj.server.implementation.battle.ability

import pl.margoj.server.implementation.battle.Battle
import pl.margoj.server.implementation.battle.BattleData
import pl.margoj.server.implementation.battle.BattleLogBuilder
import pl.margoj.server.implementation.entity.EntityImpl

class NormalStrike(battle: Battle, user: EntityImpl) : PlayerAbility(battle, user)
{
    override fun onUse(target: EntityImpl, userData: BattleData, targetData: BattleData): Boolean
    {
        if (userData.team == targetData.team)
        {
            return false
        }

        val damage = 100 // TODO
        target.damage(damage)
        targetData.updatedNow()

        val log = BattleLogBuilder()
        log.damager = userData
        log.damaged = targetData
        log.damage = damage
        log.damageTaken = damage

        battle.addLog(log.toString())
        return true
    }
}