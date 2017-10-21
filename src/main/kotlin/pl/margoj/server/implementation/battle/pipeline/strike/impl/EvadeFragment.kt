package pl.margoj.server.implementation.battle.pipeline.strike.impl

import pl.margoj.server.implementation.battle.pipeline.strike.StrikePipelineData
import pl.margoj.server.implementation.battle.pipeline.strike.StrikePipelineFragment

class EvadeFragment : StrikePipelineFragment
{
    override fun process(fragment: StrikePipelineData)
    {
        val damager = fragment.ability!!.user
        val damaged = fragment.ability!!.target
        val chance = Math.min(40.0 * Math.min(50, damaged.stats.evade).toDouble() / damager.level.toDouble() / 100.0f, 0.5)

        if (Math.random() < chance)
        {
            fragment.physicalDamage = 0
            fragment.evaded = true
            fragment.log.damageEvaded = true
        }
    }
}