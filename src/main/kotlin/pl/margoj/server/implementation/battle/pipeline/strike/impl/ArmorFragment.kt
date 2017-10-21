package pl.margoj.server.implementation.battle.pipeline.strike.impl

import pl.margoj.server.implementation.battle.pipeline.strike.StrikePipelineData
import pl.margoj.server.implementation.battle.pipeline.strike.StrikePipelineFragment
import pl.margoj.server.implementation.utils.MargoMath

class ArmorFragment : StrikePipelineFragment
{
    override fun process(fragment: StrikePipelineData)
    {
        val physicalReduction = MargoMath.calculateDamageReduction(fragment.physicalDamage, fragment.strikeAbility.target.stats.armor)
        fragment.armorPhysicalReduction = physicalReduction
        fragment.physicalDamage -= physicalReduction
    }
}