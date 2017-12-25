package pl.margoj.server.implementation.battle.pipeline.strike.impl

import pl.margoj.server.implementation.battle.pipeline.strike.StrikePipelineData
import pl.margoj.server.implementation.battle.pipeline.strike.StrikePipelineFragment

class BlockFragment : StrikePipelineFragment
{
    override fun process(fragment: StrikePipelineData)
    {
        val damager = fragment.ability!!.user
        val damaged = fragment.ability.target
        val chance = (20.0 * damaged.stats.block.toDouble() / damager.level.toDouble()) / 100.0f

        if (Math.random() < chance)
        {
            val physicalDamageReduction = Math.min(8 * (damaged.stats.block + damaged.level), fragment.physicalDamage)
            fragment.blocked = true
            fragment.blockedPhysicalAmount = physicalDamageReduction
            fragment.physicalDamage -= physicalDamageReduction
            fragment.log.damageBlocked = physicalDamageReduction

            // TODO: Magical damage
        }
    }
}