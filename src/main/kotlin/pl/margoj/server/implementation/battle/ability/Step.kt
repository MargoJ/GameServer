package pl.margoj.server.implementation.battle.ability

import pl.margoj.server.api.battle.BattleTeam
import pl.margoj.server.implementation.battle.BattleData
import pl.margoj.server.implementation.battle.BattleImpl
import pl.margoj.server.implementation.battle.BattleLogBuilder
import pl.margoj.server.implementation.entity.LivingEntityImpl
import pl.margoj.server.implementation.player.PlayerImpl

class Step(battle: BattleImpl, user: LivingEntityImpl, target: LivingEntityImpl) : BattleAbility(battle, user, target)
{
    override fun check(userData: BattleData, targetData: BattleData): Boolean
    {
        if (user !== target)
        {
            return false
        }

        if (userData.hasRangedWeapon())
        {
            (user as? PlayerImpl)?.displayAlert("Tylko wojownik z bronią białą może zrobić krok!")
            return false
        }

        if (userData.team == BattleTeam.TEAM_A && userData.row >= 4)
        {
            (user as? PlayerImpl)?.displayAlert("Nie możesz już zrobić kroku!")
            return false
        }
        else if (userData.team == BattleTeam.TEAM_B && userData.row <= 1)
        {
            (user as? PlayerImpl)?.displayAlert("Nie możesz już zrobić kroku!")
            return false
        }

        return true
    }

    override fun onUse(userData: BattleData, targetData: BattleData)
    {
        battle.addLog(BattleLogBuilder().build { it.who = userData }.build { it.step = true }.toString())
        userData.row += if (userData.team == BattleTeam.TEAM_A) 1 else -1
        userData.updatedNow()
    }
}