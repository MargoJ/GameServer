package pl.margoj.server.implementation.entity.simple

import pl.margoj.server.api.map.Location
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.entity.EntityImpl
import pl.margoj.server.implementation.entity.EntityTracker
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.player.PlayerImpl
import java.util.concurrent.TimeUnit

class GravestoneEntity(
        override val server: ServerImpl,
        override val location: Location,
        val killed: PlayerImpl,
        val killedBy: String,
        val creationDate: Long,
        val note: String
) : EntityImpl()
{
    override val name: String = "Gravestone"
    override val icon: String = ""
    override val direction: Int = 0

    override val canAnnounce: Boolean
        get() = this.creationDate + TimeUnit.MINUTES.toMillis(5) > System.currentTimeMillis()

    override val trackingRange: Int = 2
    override val neverDelete: Boolean = true

    override fun destroy()
    {
        this.server.entityManager.unregisterEntity(this)
    }

    override fun announce(tracker: EntityTracker, out: OutgoingPacket)
    {
        val array = out.getArray("rip")

        array.add(this.killed.name)
        array.add(this.killed.level)
        array.add(this.killed.stats.profession.id)
        array.add(this.location.x)
        array.add(this.location.y)
        array.add(this.creationDate / 1000L)
        array.add(this.killedBy)
        array.add(this.note)
    }

    override fun dispose(tracker: EntityTracker, out: OutgoingPacket)
    {
    }

    override fun update(tracker: EntityTracker, out: OutgoingPacket)
    {
    }
}