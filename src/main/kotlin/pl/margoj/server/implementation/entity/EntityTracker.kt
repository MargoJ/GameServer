package pl.margoj.server.implementation.entity

import pl.margoj.server.api.map.Town
import pl.margoj.server.api.player.Profession
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.player.PlayerImpl
import java.util.LinkedList

const val WINDOW_SIZE: Int = 16

class EntityTracker(val owner: PlayerImpl)
{
    private val trackedEntities_ = LinkedList<EntityImpl>()
    val trackingData = HashMap<EntityImpl, TrackingData>()
    val trackedEntities: List<EntityImpl> get() = this.trackedEntities_

    fun isTracking(entity: EntityImpl): Boolean
    {
        return this.trackedEntities_.contains(entity)
    }

    fun shouldTrack(anotherEntity: EntityImpl): Boolean
    {
        if (this.owner.location.town != anotherEntity.location.town)
        {
            return false
        }

        val town = this.owner.location.town!!

        return this.canSee(this.owner, anotherEntity, town, WINDOW_SIZE * anotherEntity.trackingRange)
    }

    private fun canSee(we: EntityImpl, they: EntityImpl, town: Town, size: Int): Boolean
    {
        if (!they.canAnnounce)
        {
            return false
        }

        if (they.neverDelete)
        {
            if (this.trackedEntities_.contains(they))
            {
                return true
            }
        }

        val our = we.location
        val their = they.location

        return Math.abs(our.x - their.x) <= this.getRequiredDistance(size, our.x, town.width)
                && Math.abs(our.y - their.y) <= this.getRequiredDistance(size, our.y, town.height)
    }

    private fun getRequiredDistance(windowSize: Int, position: Int, width: Int): Int
    {
        val half = windowSize / 2
        return half + 1 + maxOf(0, half - 1 - position, half - (width - position))
    }

    fun reset()
    {
        this.trackedEntities_.clear()
    }

    fun handlePacket(out: OutgoingPacket)
    {
        val entities = this.owner.server.entityManager.entities

        for (entity in entities)
        {
            if (entity == this.owner)
            {
                continue
            }

            if (this.isTracking(entity))
            {
                if (this.shouldTrack(entity))
                {
                    entity.update(this, out)
                }
                else
                {
                    entity.dispose(this, out)
                    this.trackedEntities_.remove(entity)
                }
            }
            else if (this.shouldTrack(entity))
            {
                entity.announce(this ,out)
                this.trackedEntities_.add(entity)
            }
        }

        val iterator = this.trackedEntities_.iterator()
        while (iterator.hasNext())
        {
            val trackedEntity = iterator.next()
            if (!entities.contains(trackedEntity))
            {
                trackedEntity.dispose(this, out)
                iterator.remove()
            }
        }
    }
}

data class TrackingData(var x: Int, var y: Int, var lastDirection: Int, var lastProfession: Profession, var lastLevel: Int)
{
    constructor(entity: PlayerImpl) : this(entity.location.x, entity.location.y, entity.direction, entity.stats.profession, entity.level)
}

fun PlayerImpl.getTrackingData(tracker: EntityTracker): TrackingData
{
    return tracker.trackingData[this]!!
}

fun PlayerImpl.setTrackingData(tracker: EntityTracker, data: TrackingData?)
{
    if (data == null)
    {
        tracker.trackingData.remove(this)
    }
    else
    {
        tracker.trackingData[this] = data
    }
}