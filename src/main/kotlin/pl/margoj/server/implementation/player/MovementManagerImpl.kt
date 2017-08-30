package pl.margoj.server.implementation.player

import pl.margoj.mrf.map.Point
import pl.margoj.server.api.events.player.PlayerMoveEvent
import pl.margoj.server.api.map.Location
import pl.margoj.server.api.player.MovementManager
import pl.margoj.server.api.utils.TimeUtils
import pl.margoj.server.implementation.map.TownImpl
import pl.margoj.server.implementation.npc.NpcType
import java.util.concurrent.ConcurrentLinkedQueue

class MovementManagerImpl(val player: PlayerImpl) : MovementManager
{
    private val queuedMoves = ConcurrentLinkedQueue<QueuedMove>()
    private var lastMove: QueuedMove? = null
    private var nextMove: QueuedMove? = null
    override val location: Location get() = this.player.data.location
    override var playerDirection: Int = 0
    var resetPosition = false

    override val canMove: Boolean
        get()
        {
            return !this.player.data.isDead && this.player.currentNpcTalk == null && this.player.currentBattle == null
        }

    fun queueMove(x: Int, y: Int, timestamp: Double)
    {
        this.queuedMoves.add(QueuedMove(x, y, timestamp))
    }

    fun processMove()
    {
        var last: QueuedMove? = null

        while (!this.queuedMoves.isEmpty())
        {
            val current = this.queuedMoves.remove()

            if (current.timestamp >= TimeUtils.getTimestampDouble())
            {
                break
            }

            if (!this.canMove)
            {
                return this.resetPosition()
            }

            if (this.lastMove != null && this.lastMove != current && this.lastMove!!.timestamp + ANTISPEEDHACK_TRIGGER > current.timestamp)
            {
                return this.resetPosition()
            }

            val newLocation = Location(this.location.town, current.x, current.y)

            if (!this.canMoveTo(newLocation))
            {
                return this.resetPosition()
            }

            val event = PlayerMoveEvent(this.player, this.location, newLocation)
            this.player.server.eventManager.call(event)

            if (event.cancelled)
            {
                return this.resetPosition()
            }

            newLocation.copyValuesTo(this.location)

            last = current
            this.lastMove = current
        }

        this.nextMove = last
    }

    private fun resetPosition()
    {
        this.queuedMoves.clear()
        this.nextMove = QueuedMove(this.location.x, this.location.y, 0.0)
        this.resetPosition = true
    }

    fun updatePosition()
    {
        this.player.connection.addModifier { it.addStatisticRecalculation(StatisticType.POSITION) }
    }

    fun clearQueue()
    {
        this.queuedMoves.clear()
    }

    fun getNextMoveAndClear(): QueuedMove?
    {
        if (this.nextMove == null)
        {
            return null
        }
        val nextMove = this.nextMove
        this.nextMove = null
        return nextMove
    }

    fun canMoveTo(newLocation: Location): Boolean
    {
        if(!this.location.isNear(newLocation))
        {
            return false
        }

        val town = this.location.town!! as TownImpl

        if(!town.inBounds(Point(newLocation.x, newLocation.y)))
        {
            player.server.gameLogger.warn("${player.name}: niedozwolone przejśćie: ${newLocation.toSimpleString()}, miejsce poza mapą!")
            return false
        }

        val canNoclip = this.player.server.debugModeEnabled // TODO: PERMISSIONS

        if(!canNoclip)
        {
            if (town.collisions[newLocation.x][newLocation.y])
            {
                return false
            }

            for (npc in town.npc)
            {
                if(npc.type == NpcType.TRANSPARENT)
                {
                    continue
                }

                if(npc.location == newLocation)
                {
                    return false
                }
            }
        }


        return true
    }

    companion object
    {
        val MOVE = 0.2
        val ANTISPEEDHACK_MARGIN = 0.01
        val ANTISPEEDHACK_TRIGGER = MOVE - ANTISPEEDHACK_MARGIN
    }
}

data class QueuedMove(val x: Int, val y: Int, val timestamp: Double)
