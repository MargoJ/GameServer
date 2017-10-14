package pl.margoj.server.implementation.battle.pipeline.strike.impl

import pl.margoj.server.implementation.battle.pipeline.strike.StrikePipelineData
import pl.margoj.server.implementation.battle.pipeline.strike.StrikePipelineFragment

class InitialDamageLogFragement : StrikePipelineFragment
{
    override fun process(fragment: StrikePipelineData)
    {
        fragment.log.damage = fragment.physicalDamage
    }
}