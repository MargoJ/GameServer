package pl.margoj.server.implementation.entity

import pl.margoj.server.api.entity.Entity
import pl.margoj.server.implementation.network.protocol.OutgoingPacket

abstract class EntityImpl : Entity
{
    open val trackingRange: Int = 1

    open val neverDelete: Boolean = false

    abstract val canAnnounce: Boolean

    abstract fun announce(tracker: EntityTracker, out: OutgoingPacket)

    abstract fun dispose(tracker: EntityTracker, out: OutgoingPacket)

    abstract fun update(tracker: EntityTracker, out: OutgoingPacket)
}