package pl.margoj.server.implementation.battle.pipeline.strike.impl

import pl.margoj.server.implementation.battle.pipeline.strike.StrikePipelineData
import pl.margoj.server.implementation.battle.pipeline.strike.StrikePipelineFragment
import java.util.concurrent.ThreadLocalRandom

class BaseDamageFragment : StrikePipelineFragment
{
    override fun process(fragment: StrikePipelineData)
    {
        val ability = fragment.ability!!
        val damageRange = ability.user.stats.damage
        val damage = ThreadLocalRandom.current().nextInt(damageRange.first, damageRange.endInclusive + 1)

        fragment.physicalDamage = damage

        fragment.log.damager = ability.battle.getDataOf(ability.user)
        fragment.log.damaged = ability.battle.getDataOf(ability.target)
    }
}