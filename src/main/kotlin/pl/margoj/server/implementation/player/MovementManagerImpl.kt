package pl.margoj.server.implementation.player

import pl.margoj.mrf.map.Point
import pl.margoj.server.api.map.Location
import pl.margoj.server.api.player.MovementManager
import pl.margoj.server.api.utils.TimeUtils
import pl.margoj.server.implementation.map.TownImpl
import java.util.concurrent.ConcurrentLinkedQueue

class MovementManagerImpl(val player: PlayerImpl) : MovementManager
{
    private val queuedMoves = ConcurrentLinkedQueue<QueuedMove>()
    private var lastMove: QueuedMove? = null
    override var location: Location = Location(null)
    override var playerDirection: Int = 0

    fun queueMove(x: Int, y: Int, timestamp: Double)
    {
        this.queuedMoves.add(QueuedMove(x, y, timestamp))
    }

    fun processMove(): QueuedMove?
    {
        var last: QueuedMove? = null

        while (!this.queuedMoves.isEmpty())
        {
            val current = this.queuedMoves.remove()

            if (current.timestamp >= TimeUtils.getTimestampDouble())
            {
                break
            }

            if (this.lastMove != null && this.lastMove != current && this.lastMove!!.timestamp + ANTISPEEDHACK_TRIGGER > current.timestamp)
            {
                this.queuedMoves.clear()
                return QueuedMove(this.location.x, this.location.y, 0.0)
            }

            val newLocation = Location(this.location.town, current.x, current.y)
            val town = this.location.town!! as TownImpl

            if (!this.location.isNear(newLocation) || town.collisions[newLocation.x][newLocation.y] || !town.map.inBounds(Point(current.x, current.y)))
            {
                this.queuedMoves.clear()
                return QueuedMove(this.location.x, this.location.y, 0.0)
            }

            newLocation.copyValuesTo(this.location)

            last = current
            this.lastMove = current
        }

        return last
    }

    fun updatePosition()
    {
        this.player.connection.addModifier { it.addMove(this.location.x, this.location.y) }
    }

    fun clearQueue()
    {
        this.queuedMoves.clear()
    }

    companion object
    {
        val MOVE = 0.2
        val ANTISPEEDHACK_MARGIN = 0.01
        val ANTISPEEDHACK_TRIGGER = MOVE - ANTISPEEDHACK_MARGIN
    }
}

data class QueuedMove(val x: Int, val y: Int, val timestamp: Double)
