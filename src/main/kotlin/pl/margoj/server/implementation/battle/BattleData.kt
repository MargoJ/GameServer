package pl.margoj.server.implementation.battle

import pl.margoj.server.api.player.Player
import pl.margoj.server.implementation.entity.EntityImpl
import pl.margoj.server.implementation.network.protocol.jsons.BattleParticipant
import pl.margoj.server.implementation.npc.Npc

class BattleData(val entity: EntityImpl, val battle: Battle, val team: Team)
{
    init
    {
        this.reset()
    }

    /** in-battle id */
    val id = (if (this.entity is Player) this.entity.id else -this.entity.id).toLong()

    /** did player press the quit button? */
    var quitRequested: Boolean = false

    /** got init packet */
    var initialized = false

    /** last received log id */
    var lastLog: Int = -1

    /** auto-fight enabled **/
    var auto: Boolean = false

    /** send update for auto var? */
    var needsAutoUpdate = true

    /** last time when a secondLeft has been decreased */
    var lastSecondUpdate = 0L

    /** how much seconds is there left for current turn */
    var secondsLeft = 0

    /** when a participant update was sent? */
    var lastUpdateSendTick = -1L

    /** when this participant was updated last time? */
    var lastUpdatedTick: Long = -1L

    /** is this participant dead? */
    var dead = false

    /** in-battle attack speed */
    var battleAttackSpeed: Double = entity.stats.attackSpeed + 1.0
        set(value)
        {
            this.battle.updateAttackSpeedThreshold()
        }

    /** turn attack speed, used to calculate whose turn is now */
    var turnAttackSpeed = 0.0

    fun reset()
    {
        this.initialized = false
        this.lastLog = -1
        this.needsAutoUpdate = true
        this.lastUpdateSendTick = -1L
    }

    fun updatedNow()
    {
        this.lastUpdatedTick = this.entity.server.ticker.currentTick
    }

    fun createBattleParticipantObject(target: EntityImpl): BattleParticipant
    {
        val obj = BattleParticipant(this.id)
        obj.name = entity.name
        obj.level = entity.level
        obj.profession = entity.stats.profession
        obj.npc = if (entity is Npc) 1 else 0
        obj.gender = if (entity is Player) entity.data.gender.id.toString() else "x"
        obj.healthPercent = if (entity.battleData!!.dead) 0 else entity.healthPercent
        obj.team = if (this.team == Team.TEAM_A) 1 else 2
        obj.row = if (this.team == Team.TEAM_A) 2 else 3 // TODO

        if (target == this.entity)
        {
            obj.energy = 123 // TODO
            obj.mana = 456 // TODO
            obj.fast = if (this.auto) 1 else 0
        }

        obj.icon = this.entity.icon
        obj.buffs = 0 // TODO

        return obj
    }

    enum class Team
    {
        TEAM_A,
        TEAM_B
    }
}