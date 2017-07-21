package pl.margoj.server.implementation.entity

import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.network.protocol.jsons.OtherObject
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
        val our = this.owner.location
        val their = anotherEntity.location

        return Math.abs(our.x - their.x) <= this.getRequiredDistance(WINDOW_SIZE, our.x, town.width)
                && Math.abs(our.y - their.y) <= this.getRequiredDistance(WINDOW_SIZE, our.y, town.height)
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
                    this.updateEntity(entity, out)
                }
                else
                {
                    this.disposeEntity(entity, out)
                    this.trackedEntities_.remove(entity)
                }
            }
            else if (this.shouldTrack(entity))
            {
                this.annouceEntity(entity, out)
                this.trackedEntities_.add(entity)
            }
        }

        val iterator = this.trackedEntities_.iterator()
        while (iterator.hasNext())
        {
            val trackedEntity = iterator.next()
            if (!entities.contains(trackedEntity))
            {
                this.disposeEntity(trackedEntity, out)
                iterator.remove()
            }
        }
    }

    private fun annouceEntity(entity: EntityImpl, out: OutgoingPacket)
    {
        val other = OtherObject()
        other.id = entity.id

        if (entity is PlayerImpl)
        {
            other.nick = entity.name
            other.icon = entity.data.icon
            other.clan = ""
            other.x = entity.location.x
            other.y = entity.location.y
            other.direction = entity.movementManager.playerDirection
            other.rights = 0
            other.level = entity.data.level
            other.profession = entity.data.profession
            other.attributes = 0
            other.relation = ""

            entity.setTrackingData(this, TrackingData(entity))
        }
        else
        {
            // TODO
        }

        out.addOther(other)
    }

    private fun disposeEntity(entity: EntityImpl, out: OutgoingPacket)
    {
        entity.setTrackingData(this, null)

        val other = OtherObject()
        other.id = entity.id
        other.del = 1

        out.addOther(other)
    }

    private fun updateEntity(entity: EntityImpl, out: OutgoingPacket)
    {
        val trackingData = entity.getTrackingData(this)
        var updated = false
        val other = OtherObject()
        other.id = entity.id

        val location = entity.location

        if (trackingData.x != location.x || trackingData.y != location.y)
        {
            updated = true
            other.x = location.x
            other.y = location.y
            trackingData.x = location.x
            trackingData.y = location.y
        }

        if (trackingData.lastDirection != entity.direction)
        {
            updated = true
            other.direction = entity.direction
            trackingData.lastDirection = entity.direction
        }

        if (updated)
        {
            out.addOther(other)
        }
    }
}

data class TrackingData(var x: Int, var y: Int, var lastDirection: Int)
{
    constructor(entity: EntityImpl) : this(entity.location.x, entity.location.y, entity.direction)
}

fun EntityImpl.getTrackingData(tracker: EntityTracker): TrackingData
{
    return tracker.trackingData[this]!!
}

fun EntityImpl.setTrackingData(tracker: EntityTracker, data: TrackingData?)
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