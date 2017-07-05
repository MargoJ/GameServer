package pl.margoj.server.implementation.threads

import pl.margoj.server.api.sync.Tickable
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.player.PlayerImpl

class PlayerKeepAlive(val server: ServerImpl, keepAliveSeconds: Int) : Tickable
{
    private val keepAlive: Long = keepAliveSeconds.toLong() * 1000L

    override fun tick(currentTick: Long)
    {
        if (this.server.players.isEmpty())
        {
            return
        }

        val iterator = this.server.players.iterator() as MutableIterator<PlayerImpl>

        while(iterator.hasNext())
        {
            val player = iterator.next()

            if (player.connection.lastPacket != 0L && player.connection.lastPacket + keepAlive < System.currentTimeMillis())
            {
                player.connection.dispose()
                player.server.networkManager.resetPlayerConnection(player.connection)
                iterator.remove()
                player.server.entityManager.unregisterEntity(player)
                server.logger.debug("Timed out: ${player.name}")
            }
        }
    }
}