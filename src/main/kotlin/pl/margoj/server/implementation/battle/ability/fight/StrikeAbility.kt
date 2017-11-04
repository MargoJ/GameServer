package pl.margoj.server.implementation.battle.ability.fight

import pl.margoj.server.implementation.battle.BattleData
import pl.margoj.server.implementation.battle.BattleImpl
import pl.margoj.server.implementation.battle.ability.BattleAbility
import pl.margoj.server.implementation.battle.pipeline.BattlePipelines
import pl.margoj.server.implementation.battle.pipeline.strike.StrikePipelineData
import pl.margoj.server.implementation.entity.LivingEntityImpl
import pl.margoj.server.implementation.player.PlayerImpl

abstract class StrikeAbility(battle: BattleImpl, user: LivingEntityImpl, target: LivingEntityImpl) : BattleAbility(battle, user, target)
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
        val data = StrikePipelineData(this)
        BattlePipelines.STRIKE_PIPELINE.process(data)
        BattleImpl.logger.trace("${userData.battle.battleId}: strike result: $data")

        userData.battle.addLog(data.log.toString())
    }
}