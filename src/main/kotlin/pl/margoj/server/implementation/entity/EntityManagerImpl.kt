package pl.margoj.server.implementation.entity

import pl.margoj.server.api.entity.Entity
import pl.margoj.server.api.entity.EntityManager
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.player.PlayerImpl
import java.util.LinkedList

class EntityManagerImpl(server: ServerImpl): EntityManager
{
    private val entities_ = HashMap<Int, EntityImpl>()
    private val players_ = LinkedList<PlayerImpl>()

    override val entities: Collection<EntityImpl>
        get() = this.entities_.values

    override val players: Collection<PlayerImpl>
        get() = this.players_

    fun registerEntity(entity: EntityImpl)
    {
        if (entity is PlayerImpl)
        {
            this.players_.add(entity)
        }
        this.entities_.put(entity.id, entity)
    }

    fun unregisterEntity(entity: EntityImpl)
    {
        if (entity is PlayerImpl)
        {
            this.players_.remove(entity)
        }
        this.entities_.remove(entity.id)
    }

    override fun getEntityById(id: Int): Entity?
    {
        return this.entities_[id]
    }
}