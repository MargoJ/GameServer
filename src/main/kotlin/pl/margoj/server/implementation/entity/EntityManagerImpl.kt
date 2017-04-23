package pl.margoj.server.implementation.entity

import pl.margoj.server.api.entity.EntityManager
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.player.PlayerImpl
import java.util.LinkedList

class EntityManagerImpl(server: ServerImpl): EntityManager
{
    private val entities_ = LinkedList<EntityImpl>()
    private val players_ = LinkedList<PlayerImpl>()

    override val entities: Collection<EntityImpl>
        get() = this.entities_

    override val players: Collection<PlayerImpl>
        get() = this.players_

    fun registerEntity(entity: EntityImpl): Boolean
    {
        if (entity is PlayerImpl)
        {
            this.players_.add(entity)
        }
        return this.entities_.add(entity)
    }

    fun unregisterEntity(entity: EntityImpl): Boolean
    {
        if (entity is PlayerImpl)
        {
            this.players_.remove(entity)
        }
        return this.entities_.remove(entity)
    }
}