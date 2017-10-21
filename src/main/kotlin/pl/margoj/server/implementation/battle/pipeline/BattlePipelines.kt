package pl.margoj.server.implementation.battle.pipeline

import pl.margoj.server.implementation.battle.pipeline.move.MovePipelineData
import pl.margoj.server.implementation.battle.pipeline.move.impl.BuffExpireFragment
import pl.margoj.server.implementation.battle.pipeline.move.impl.CalculateSAFragment
import pl.margoj.server.implementation.battle.pipeline.strike.StrikePipelineData
import pl.margoj.server.implementation.battle.pipeline.strike.impl.*

object BattlePipelines
{
    val MOVE_PIPELINE = BattlePipeline<MovePipelineData>()
    val STRIKE_PIPELINE = BattlePipeline<StrikePipelineData>()

    init
    {
        MOVE_PIPELINE.addLast("MJ|CalculateSA", CalculateSAFragment())
        MOVE_PIPELINE.addLast("MJ|BuffExpire", BuffExpireFragment())

        STRIKE_PIPELINE.addLast("MJ|BaseDamage", BaseDamageFragment())
        STRIKE_PIPELINE.addLast("MJ|InitialDamageLog", InitialDamageLogFragement())
        STRIKE_PIPELINE.addLast("MJ|Armor", ArmorFragment())
        STRIKE_PIPELINE.addLast("MJ|TestBuffModifier", TestBuffModifierFragment())
        STRIKE_PIPELINE.addLast("MJ|Evade", EvadeFragment())
        STRIKE_PIPELINE.addLast("MJ|Block", BlockFragment())
        STRIKE_PIPELINE.addLast("MJ|FinalDamageLog", FinalDamageFragement())
    }
}