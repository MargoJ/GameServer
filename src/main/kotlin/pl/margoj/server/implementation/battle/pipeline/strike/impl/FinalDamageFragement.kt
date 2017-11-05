package pl.margoj.server.implementation.battle.pipeline.strike.impl

import pl.margoj.server.implementation.battle.pipeline.strike.StrikePipelineData
import pl.margoj.server.implementation.battle.pipeline.strike.StrikePipelineFragment

class FinalDamageFragement : StrikePipelineFragment
{
    override fun process(fragment: StrikePipelineData)
    {
        fragment.log.damageTaken = fragment.physicalDamage

        val target = fragment.ability!!.target
        target.damage(fragment.physicalDamage, fragment.strikeAbility.user)

        fragment.ability.battle.getDataOf(target)!!.updatedNow()
    }
}