package pl.margoj.server.implementation.tasks

import pl.margoj.server.api.entity.Entity
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.entity.simple.GravestoneEntity
import java.util.concurrent.TimeUnit

class GravestoneCleanupTask(val server: ServerImpl) : Runnable
{
    override fun run()
    {
        val iterator = this.server.entityManager.entities.iterator() as MutableIterator<Entity>

        while (iterator.hasNext())
        {
            val gravestone = iterator.next() as? GravestoneEntity ?: continue

            if (gravestone.creationDate + TimeUnit.MINUTES.toMillis(5L) < System.currentTimeMillis())
            {
                iterator.remove()
            }
        }
    }
}