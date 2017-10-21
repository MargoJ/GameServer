package pl.margoj.server.implementation.battle.pipeline.move.impl

import pl.margoj.server.implementation.battle.pipeline.move.MovePipelineData
import pl.margoj.server.implementation.battle.pipeline.move.MovePipelineFragment

class CalculateSAFragment : MovePipelineFragment
{
    override fun process(fragment: MovePipelineData)
    {
        if(fragment.position == MovePipelineData.Position.MOVE_START)
        {
            fragment.data.battleAttackSpeed = fragment.data.entity.stats.attackSpeed + 1.0
        }
    }
}