package pl.margoj.server.implementation.battle.pipeline

import pl.margoj.server.implementation.battle.pipeline.strike.StrikePipelineData
import pl.margoj.server.implementation.battle.pipeline.strike.impl.*

object BattlePipelines
{
    val STRIKE_PIPELINE = BattlePipeline<StrikePipelineData>()

    init
    {
        STRIKE_PIPELINE.addLast("MJ|BaseDamage", BaseDamageFragment())
        STRIKE_PIPELINE.addLast("MJ|InitialDamageLog", InitialDamageLogFragement())
        STRIKE_PIPELINE.addLast("MJ|Armor", ArmorFragment())
        STRIKE_PIPELINE.addLast("MJ|Evade", EvadeFragment())
        STRIKE_PIPELINE.addLast("MJ|Block", BlockFragment())
        STRIKE_PIPELINE.addLast("MJ|FinalDamageLog", FinalDamageFragement())
    }
}