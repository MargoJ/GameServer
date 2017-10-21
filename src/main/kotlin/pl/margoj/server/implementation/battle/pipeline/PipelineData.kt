package pl.margoj.server.implementation.battle.pipeline

import pl.margoj.server.implementation.battle.BattleLogBuilder
import pl.margoj.server.implementation.battle.ability.BattleAbility

open class PipelineData(var ability: BattleAbility?)
{
    val log = BattleLogBuilder()
}