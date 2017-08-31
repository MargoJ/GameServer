package pl.margoj.server.implementation.battle

import org.apache.commons.lang3.Validate
import pl.margoj.server.api.battle.Battle
import pl.margoj.server.api.battle.BattleTeam
import pl.margoj.server.api.events.battle.BattleFinishedEvent
import pl.margoj.server.api.events.battle.BattleStartedEvent
import pl.margoj.server.api.events.battle.BattleStartingEvent
import pl.margoj.server.api.player.Player
import pl.margoj.server.api.player.Profession
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.battle.ability.BattleAbility
import pl.margoj.server.implementation.battle.ability.NormalStrike
import pl.margoj.server.implementation.battle.ability.Step
import pl.margoj.server.implementation.entity.EntityImpl
import pl.margoj.server.implementation.npc.Npc
import pl.margoj.server.implementation.player.PlayerImpl
import pl.margoj.server.implementation.utils.MargoMath
import java.util.Collections

class BattleImpl internal constructor(val server: ServerImpl, override val teamA: List<EntityImpl>, override val teamB: List<EntityImpl>) : Battle
{
    override val participants: List<EntityImpl>

    /**status **/
    override var started = false
        private set
    override var finished = false
        private set
    override var winner: BattleTeam? = null
        private set
    override var loser: BattleTeam? = null
        private set

    /* update */
    var lastProcessTick = -1L

    /* data */
    private var queuedMove: BattleAbility? = null
    private var attackSpeedThreshold: Double = -1.0
    private var currentTurnOrder = mutableListOf<EntityImpl>()

    var currentEntity: EntityImpl? = null
        private set

    var currentTurn = 0
        private set

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

    internal fun start()
    {
        val event = BattleStartingEvent(this)
        server.eventManager.call(event)
        if (event.cancelled)
        {
            return
        }

        for (participant in this.participants)
        {
            if (participant is Npc)
            {
                participant.hp = participant.stats.maxHp
            }

            participant.currentBattle = this

            val team = if (teamA.contains(participant)) BattleTeam.TEAM_A else BattleTeam.TEAM_B
            val data = BattleData(participant, this, team)

            var row: Int

            if (!data.hasRangedWeapon())
            {
                row = 2
            }
            else
            {
                row = when (participant.stats.profession)
                {
                    Profession.WARRIOR, Profession.PALADIN, Profession.BLADE_DANCER -> 2
                    Profession.MAGE -> 1
                    Profession.HUNTER, Profession.TRACKER -> 0
                }
            }

            if (team == BattleTeam.TEAM_B)
            {
                row = 5 - row
            }
            data.row = row

            participant.battleData = data
        }

        this.updateAttackSpeedThreshold()

        this.started = true

        server.eventManager.call(BattleStartedEvent(this))
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
        while (true)
        {
            if (this.queuedMove != null)
            {
                if (this.isAbilityValid(this.queuedMove!!))
                {
                    this.queuedMove!!.performNow()
                }

                this.queuedMove = null
            }

            this.updateCurrent()

            if (this.checkFinishCondition())
            {
                this.finish()
                return
            }

            val canContinue = this.processOne()

            if (this.checkFinishCondition())
            {
                this.finish()
                return
            }

            if (!canContinue)
            {
                break
            }
        }
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

            data.secondsLeft = data.startsMove
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
                var auto = entity.battleData!!.auto

                if (!auto && entity.battleData!!.secondsLeft <= 0)
                {
                    auto = true

                    if (entity.battleData!!.startsMove >= 10) // TODO
                    {
                        entity.battleData!!.startsMove -= 5
                    }
                }
                if (auto)
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
        var target: EntityImpl? = null
        this.iterateOverAlive { participant ->
            if (entity.battleData!!.team == participant.battleData!!.team || !entity.battleData!!.canReach(participant.battleData!!.row))
            {
                return@iterateOverAlive
            }

            if (target == null)
            {
                target = participant
                return@iterateOverAlive
            }

            val isTargetMelee = !target!!.battleData!!.hasRangedWeapon()
            val isParticipantMelee = !participant.battleData!!.hasRangedWeapon()

            if (!isTargetMelee && isParticipantMelee)
            {
                target = participant
                return@iterateOverAlive
            }
            else if (isTargetMelee && !isParticipantMelee)
            {
                return@iterateOverAlive
            }

            if (participant.level < target!!.level)
            {
                target = participant
            }
        }

        if (target == null)
        {
            this.performOrError(Step(this, entity, entity), { "${entity.name}: step failed" })
        }
        else
        {
            this.performOrError(NormalStrike(this, entity, target!!), { "${entity.name}: target failed: ${target!!.name}" })
        }
    }

    fun queueMove(ability: BattleAbility)
    {
        if (this.isAbilityValid(ability))
        {
            if (ability.user is Player)
            {
                ability.user.battleData!!.startsMove = 15 // TODO
            }

            this.queuedMove = ability
        }
    }

    fun isAbilityValid(ability: BattleAbility): Boolean
    {
        if (this.finished || ability.user.currentBattle != this || ability.target.currentBattle != this)
        {
            return false
        }
        if (ability.user.battleData!!.dead || ability.target.battleData!!.dead)
        {
            return false
        }
        if (this.currentEntity != ability.user)
        {
            return false
        }
        return true
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
        for (participant in this.participants)
        {
            if (participant.currentBattle == this)
            {
                participant.battleData!!.addLog(log)
            }
        }
    }

    private fun performOrError(ability: BattleAbility, error: () -> String)
    {
        if (!ability.performNow())
        {
            this.addLog(BattleLogBuilder().build { it.text = error() }.toString())
            this.moveDone(ability.user);
        }
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

        if (this.isEveryoneDead(BattleTeam.TEAM_A))
        {
            this.loser = BattleTeam.TEAM_A
            this.winner = BattleTeam.TEAM_B
        }
        else if (this.isEveryoneDead(BattleTeam.TEAM_B))
        {
            this.loser = BattleTeam.TEAM_B
            this.winner = BattleTeam.TEAM_A
        }

        return this.winner != null
    }

    private fun isEveryoneDead(team: BattleTeam): Boolean
    {
        val list = this.getTeamParticipants(team)

        for (entity in list)
        {
            if (entity.currentBattle == this && !entity.battleData!!.dead)
            {
                return false
            }
        }

        return true
    }

    override fun getTeamParticipants(team: BattleTeam): List<EntityImpl>
    {
        return when (team)
        {
            BattleTeam.TEAM_A -> this.teamA
            BattleTeam.TEAM_B -> this.teamB
        }
    }

    private fun finish()
    {
        val log = BattleLogBuilder()

        log.winner = when (this.winner)
        {
            BattleTeam.TEAM_A -> this.teamA
            BattleTeam.TEAM_B -> this.teamB
            null -> Collections.emptyList()
        }

        this.addLog(log.toString())

        this.calcExp()

        this.finished = true

        val event = BattleFinishedEvent(
                battle = this,
                hasWinner = this.winner != null,
                winnerTeam = this.winner,
                loserTeam = this.loser
        )
        this.server.eventManager.call(event)
    }

    private fun calcExp()
    {
        if (this.winner == null || this.loser == null)
        {
            return
        }

        val areWinnersNpcs = this.getTeamParticipants(this.winner!!).all { it is Npc }
        val areLosersNpcs = this.getTeamParticipants(this.loser!!).all { it is Npc }

        if (areWinnersNpcs)
        {
            return
        }

        if (areLosersNpcs)
        {
            val winnerTeam = this.getTeamParticipants(this.winner!!)
            var winnerTeamLevelAverage = 0
            for (winner in winnerTeam)
            {
                winnerTeamLevelAverage += winner.level
            }
            winnerTeamLevelAverage /= winnerTeam.size

            var totalExp = 0L

            // TODO

            var averageHpPercent = 0L
            for (winner in winnerTeam)
            {
                averageHpPercent += winner.healthPercent
            }
            averageHpPercent /= winnerTeam.size

            val hpLost = (100 - averageHpPercent)
            val hpBonus = (hpLost.toDouble() / 15.0).toInt() * 0.10

            for (npc in this.getTeamParticipants(this.loser!!))
            {
                var baseExp: Long

                if (npc.level >= winnerTeamLevelAverage)
                {
                    baseExp = MargoMath.baseExpFromMob(npc.level)
                }
                else if (npc.level + 25 > winnerTeamLevelAverage)
                {
                    baseExp = MargoMath.baseExpFromMob(npc.level)
                    val advantage = winnerTeamLevelAverage - npc.level
                    val reductionPercent = advantage.toDouble() * 0.04
                    val reductionExp = (reductionPercent * baseExp.toDouble()).toInt()

                    totalExp += baseExp - reductionExp
                }
                else
                {
                    continue
                }

                totalExp = baseExp + (hpBonus * baseExp).toLong()

                // TODO: elite2, hero
            }

            for (winner in winnerTeam)
            {
                // TODO
                val winnerXp = totalExp

                if (winner.currentBattle == this)
                {
                    winner.battleData!!.addLog(BattleLogBuilder().build { it.expGained = winnerXp }.toString())
                }

                (winner as? PlayerImpl)?.data?.addExp(winnerXp)
            }
        }
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