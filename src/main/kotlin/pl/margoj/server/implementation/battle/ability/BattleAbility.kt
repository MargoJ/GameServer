package pl.margoj.server.implementation.battle.ability

import pl.margoj.server.implementation.battle.BattleData
import pl.margoj.server.implementation.battle.BattleImpl
import pl.margoj.server.implementation.battle.pipeline.BattlePipelines
import pl.margoj.server.implementation.battle.pipeline.move.MovePipelineData
import pl.margoj.server.implementation.entity.LivingEntityImpl

abstract class BattleAbility(val battle: BattleImpl, val user: LivingEntityImpl, val target: LivingEntityImpl)
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
            BattleImpl.logger.trace("${this.battle.battleId}: peformNow ${this}")

            BattlePipelines.MOVE_PIPELINE.process(MovePipelineData(this.user, this.battle.getDataOf(this.user)!!, MovePipelineData.Position.MOVE_START))
            this.onUse(user.battleData!!, target.battleData!!)
            battle.moveDone(this.user)
        }

        return result
    }

    protected abstract fun check(userData: BattleData, targetData: BattleData): Boolean

    protected abstract fun onUse(userData: BattleData, targetData: BattleData)
}