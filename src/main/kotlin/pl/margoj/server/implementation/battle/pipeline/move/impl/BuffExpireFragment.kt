package pl.margoj.server.implementation.battle.pipeline.move.impl

import pl.margoj.server.implementation.battle.BattleImpl
import pl.margoj.server.implementation.battle.pipeline.move.MovePipelineData
import pl.margoj.server.implementation.battle.pipeline.move.MovePipelineFragment

class BuffExpireFragment : MovePipelineFragment
{
    override fun process(fragment: MovePipelineData)
    {
        if(fragment.position == MovePipelineData.Position.TURN_END)
        {
            for (buff in fragment.data.buffs)
            {
                if(fragment.data.battle.currentTurn >= buff.activeUntil)
                {
                    BattleImpl.logger.trace("${fragment.data.battle.battleId}: buffExpired: $buff")
                    fragment.data.removeBuff(buff)
                }
            }
        }
    }
}