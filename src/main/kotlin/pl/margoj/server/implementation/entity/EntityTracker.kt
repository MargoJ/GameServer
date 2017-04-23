package pl.margoj.server.implementation.entity

import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.network.protocol.jsons.OtherObject
import pl.margoj.server.implementation.player.PlayerImpl
import java.util.LinkedList

class EntityTracker(val owner: PlayerImpl)
{
    private val trackedEntities_ = LinkedList<EntityImpl>()
    val trackingData = HashMap<EntityImpl, TrackingData>()
    var trackingRange = owner.server.config.gameConfig.trackingRange
    val trackedEntities: List<EntityImpl> get() = this.trackedEntities_

    fun isTracking(entity: EntityImpl): Boolean
    {
        return this.trackedEntities_.contains(entity)
    }

    fun shouldTrack(anotherEntity: EntityImpl): Boolean
    {
        return anotherEntity.location.town == this.owner.location.town && anotherEntity.location.distance(this.owner.location) <= this.trackingRange
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
                }
            }
            else if (this.shouldTrack(entity))
            {
                this.annouceEntity(entity, out)
            }
        }

        for (trackedEntity in ArrayList(this.trackedEntities_))
        {
            if(!entities.contains(trackedEntity))
            {
                this.disposeEntity(trackedEntity, out)
            }
        }
    }

    fun annouceEntity(entity: EntityImpl, out: OutgoingPacket)
    {
        this.trackedEntities_.add(entity)

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

    fun disposeEntity(entity: EntityImpl, out: OutgoingPacket)
    {
        this.trackedEntities_.remove(entity)
        entity.setTrackingData(this, null)

        val other = OtherObject()
        other.id = entity.id
        other.del = 1

        out.addOther(other)
    }

    fun updateEntity(entity: EntityImpl, out: OutgoingPacket)
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