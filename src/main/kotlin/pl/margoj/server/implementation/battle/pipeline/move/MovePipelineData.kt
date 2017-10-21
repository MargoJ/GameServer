package pl.margoj.server.implementation.battle.pipeline.move

import pl.margoj.server.implementation.battle.BattleData
import pl.margoj.server.implementation.battle.pipeline.PipelineData
import pl.margoj.server.implementation.entity.EntityImpl

class MovePipelineData(val user: EntityImpl, val data: BattleData, val position: Position) : PipelineData(null)
{
    enum class Position
    {
        MOVE_START,
        MOVE_END,
        TURN_END
    }
}