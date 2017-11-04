package pl.margoj.server.implementation.entity

import pl.margoj.server.api.entity.EntityManager
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.npc.Npc
import pl.margoj.server.implementation.player.PlayerImpl

class EntityManagerImpl(server: ServerImpl) : EntityManager
{
    private val npcs_ = HashMap<Int, Npc>()
    private val players_ = HashMap<Int, PlayerImpl>()
    private val entities_ = ArrayList<EntityImpl>()

    override val npcs: Collection<EntityImpl>
        get() = this.npcs_.values

    override val players: Collection<PlayerImpl>
        get() = this.players_.values

    override val entities: Collection<EntityImpl>
        get() = this.entities_

    fun registerEntity(entity: EntityImpl)
    {
        if (entity is PlayerImpl)
        {
            if(this.players_.containsKey(entity.id))
            {
                throw IllegalArgumentException("This entity is already registered")
            }

            this.players_.put(entity.id, entity)
        }
        else if(entity is Npc)
        {
            this.npcs_.put(entity.id, entity)
        }

        this.entities_.add(entity)
    }

    fun unregisterEntity(entity: EntityImpl)
    {
        if (entity is PlayerImpl)
        {
            this.players_.remove(entity.id)
        }
        else if(entity is Npc)
        {
            this.npcs_.remove(entity.id)
        }
        else
        {
            throw IllegalArgumentException("invalid entity $entity")
        }

        this.entities_.remove(entity)
    }

    override fun getPlayerById(id: Int): PlayerImpl?
    {
        return this.players_[id]
    }

    override fun getNpcById(id: Int): Npc?
    {
        return this.npcs_[id]
    }
}