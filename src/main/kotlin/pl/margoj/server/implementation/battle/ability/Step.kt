package pl.margoj.server.implementation.battle.ability

import pl.margoj.server.implementation.battle.Battle
import pl.margoj.server.implementation.battle.BattleData
import pl.margoj.server.implementation.battle.BattleLogBuilder
import pl.margoj.server.implementation.entity.EntityImpl
import pl.margoj.server.implementation.player.PlayerImpl

class Step(battle: Battle, user: EntityImpl, target: EntityImpl) : BattleAbility(battle, user, target)
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

        if (userData.team == BattleData.Team.TEAM_A && userData.row >= 4)
        {
            (user as? PlayerImpl)?.displayAlert("Nie możesz już zrobić kroku!")
            return false
        }
        else if (userData.team == BattleData.Team.TEAM_B && userData.row <= 1)
        {
            (user as? PlayerImpl)?.displayAlert("Nie możesz już zrobić kroku!")
            return false
        }

        return true
    }

    override fun onUse(userData: BattleData, targetData: BattleData)
    {
        battle.addLog(BattleLogBuilder().build { it.who = userData }.build { it.step = true }.toString())
        userData.row += if (userData.team == BattleData.Team.TEAM_A) 1 else -1
        userData.updatedNow()
    }
}