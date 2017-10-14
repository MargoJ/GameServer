package pl.margoj.server.implementation.battle.pipeline.strike

import pl.margoj.server.implementation.battle.ability.fight.StrikeAbility
import pl.margoj.server.implementation.battle.pipeline.PipelineData

class StrikePipelineData(val strikeAbility: StrikeAbility) : PipelineData(strikeAbility)
{
    var armorPhysicalReduction = 0

    var blocked = false
    var blockedPhysicalAmount = 0

    var evaded = false

    var physicalDamage = 0
}