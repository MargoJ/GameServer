package pl.margoj.server.implementation.battle

import org.apache.commons.lang3.Validate
import pl.margoj.server.api.player.Player
import pl.margoj.server.implementation.battle.ability.NormalStrike
import pl.margoj.server.implementation.entity.EntityImpl
import pl.margoj.server.implementation.npc.Npc
import java.util.Collections

class Battle(val teamA: List<EntityImpl>, val teamB: List<EntityImpl>)
{
    val participants: List<EntityImpl>

    /**status **/
    var started = false
        private set
    var finished = false
        private set
    var winner: BattleData.Team? = null
        private set

    /* log */
    var log: MutableMap<Int, String> = hashMapOf()
    var logCount = 0
        private set

    /* update */
    var lastProcessTick = -1L

    /* data */
    var currentTurn = 0
    var currentEntity: EntityImpl? = null
    var currentTurnOrder = mutableListOf<EntityImpl>()
    var attackSpeedThreshold: Double = -1.0

    init
    {
        Validate.notEmpty(teamA, "Team A is empty")
        Validate.notEmpty(teamB, "Team B is empty")

        val participants = ArrayList<EntityImpl>(this.teamA.size + this.teamB.size)
        participants.addAll(this.teamA)
        participants.addAll(this.teamB)
        this.participants = participants
    }


    fun findById(targetId: Long): EntityImpl?
    {
        for (participant in this.participants)
        {
            if (participant.currentBattle == this && participant.battleData!!.id == targetId)
            {
                return participant
            }
        }
        return null
    }

    fun start()
    {
        for (participant in this.participants)
        {
            if (participant is Npc)
            {
                participant.hp = participant.stats.maxHp
            }

            participant.currentBattle = this

            val battleData = BattleData(
                    entity = participant,
                    battle = this,
                    team = if (teamA.contains(participant)) BattleData.Team.TEAM_A else BattleData.Team.TEAM_B
            )

            participant.battleData = battleData
        }

        this.updateAttackSpeedThreshold()

        this.started = true
    }

    fun updateAttackSpeedThreshold()
    {
        val previous = this.attackSpeedThreshold
        var maxAttackSpeed = Double.MIN_VALUE

        this.iterateOverAlive { participant ->
            maxAttackSpeed = Math.max(maxAttackSpeed, participant.battleData!!.battleAttackSpeed)
        }

        if (previous != maxAttackSpeed)
        {
            val changeFactor = maxAttackSpeed / previous

            for (participant in this.participants)
            {
                participant.battleData!!.turnAttackSpeed /= changeFactor
            }
        }

        this.attackSpeedThreshold = maxAttackSpeed
    }

    fun processTurn()
    {
        do
        {
            if (this.checkFinishCondition())
            {
                this.finish()
                return
            }

            this.updateCurrent()
        }
        while (this.processOne())
    }

    private fun updateCurrent()
    {
        if (this.currentEntity != null)
        {
            if (this.currentEntity is Npc)
            {
                return
            }

            if (this.currentEntity is Player)
            {
                val playerData = this.currentEntity!!.battleData!!

                if (playerData.auto)
                {
                    return
                }

                if (playerData.lastSecondUpdate + 1000L <= System.currentTimeMillis())
                {
                    playerData.lastSecondUpdate = System.currentTimeMillis()
                    playerData.secondsLeft--
                }
            }
        }

        if (this.currentEntity == null)
        {
            if (this.currentTurnOrder.isEmpty())
            {
                this.turnDone()
                this.calculateTurnOrder()
            }

            if (this.currentTurnOrder.isEmpty())
            {
                return
            }

            val first = this.currentTurnOrder[0]
            this.currentTurnOrder.removeAt(0)
            this.currentEntity = first
        }
    }

    private fun calculateTurnOrder()
    {
        this.currentTurnOrder.clear()
        this.iterateOverAlive { participant ->
            val data = participant.battleData!!

            if (data.turnAttackSpeed > this.attackSpeedThreshold)
            {
                this.currentTurnOrder.add(participant)
            }

            data.secondsLeft = 5 // TODO
        }
    }

    /**
     * @return if we can process new one instantly
     */
    private fun processOne(): Boolean
    {
        val entity = this.currentEntity

        when (entity)
        {
            is Npc ->
            {
                this.processAuto(entity)
                return true
            }
            is Player ->
            {
                if (entity.battleData!!.secondsLeft <= 0 || entity.battleData!!.auto)
                {
                    this.processAuto(entity)
                    return true
                }
                return false
            }
            null -> return false
            else -> throw IllegalStateException("unknown entity: $entity")
        }
    }

    fun processAuto(entity: EntityImpl)
    {
        // TODO
        val strike = NormalStrike(this, entity)

        this.iterateOverAlive { participant ->
            if (participant.battleData!!.team != entity.battleData!!.team)
            {
                strike.use(participant)
                return@processAuto
            }
        }

        this.addLog(BattleLogBuilder().build { it.text = "${entity.name}: utrata kolejki" }.toString())
        this.moveDone(entity)
    }

    fun moveDone(entity: EntityImpl)
    {
        Validate.isTrue(entity == this.currentEntity, "Invalid entity")
        this.currentEntity = null

        entity.battleData!!.turnAttackSpeed -= this.attackSpeedThreshold

        this.iterateOverAlive { participant ->
            if (participant.hp <= 0)
            {
                participant.hp = 0
                participant.battleData!!.dead = true
            }
        }
    }

    fun turnDone()
    {
        this.currentTurn++

        this.iterateOverAlive { participant ->
            participant.battleData!!.turnAttackSpeed += participant.battleData!!.battleAttackSpeed
        }
    }

    fun addLog(log: String)
    {
        this.log.put(this.logCount++, log)
    }

    private fun checkFinishCondition(): Boolean
    {
        if (this.currentTurn >= 1000) // TODO
        {
            return true
        }

        if (this.winner != null)
        {
            return true
        }

        this.winner = when
        {
            this.isEveryoneDead(BattleData.Team.TEAM_A) -> BattleData.Team.TEAM_B
            this.isEveryoneDead(BattleData.Team.TEAM_B) -> BattleData.Team.TEAM_A
            else -> null
        }

        return this.winner != null
    }

    private fun isEveryoneDead(team: BattleData.Team): Boolean
    {
        val list = when (team)
        {
            BattleData.Team.TEAM_A -> this.teamA
            BattleData.Team.TEAM_B -> this.teamB
        }

        for (entity in list)
        {
            if (entity.currentBattle == this && !entity.battleData!!.dead)
            {
                return false
            }
        }

        return true
    }

    private fun finish()
    {
        val log = BattleLogBuilder()

        log.winner = when (this.winner)
        {
            BattleData.Team.TEAM_A -> this.teamA
            BattleData.Team.TEAM_B -> this.teamB
            null -> Collections.emptyList()
        }

        this.addLog(log.toString())

        this.finished = true
    }

    inline fun iterateOverAlive(consumer: (EntityImpl) -> Unit)
    {
        for (participant in this.participants)
        {
            if (participant.currentBattle != this || participant.battleData!!.dead)
            {
                continue
            }

            consumer(participant)
        }
    }
}