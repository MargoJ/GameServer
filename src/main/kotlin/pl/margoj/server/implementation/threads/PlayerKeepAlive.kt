package pl.margoj.server.implementation.threads

import pl.margoj.server.api.sync.Tickable
import pl.margoj.server.implementation.ServerImpl

class PlayerKeepAlive(val server: ServerImpl, keepAliveSeconds: Int) : Tickable
{
    private val keepAlive: Long = keepAliveSeconds.toLong() * 1000L

    override fun tick(currentTick: Long)
    {
        if (this.server.players.isEmpty())
        {
            return
        }
        ArrayList(this.server.players).forEach {
            player ->
            if (player.connection.lastPacket != 0L && player.connection.lastPacket + keepAlive < System.currentTimeMillis())
            {
                player.connection.dispose()
                player.server.networkManager.resetPlayerConnection(player.connection)
                player.server.entityManager.unregisterEntity(player)
                server.logger.debug("Timed out: ${player.name}")
            }
        }
    }
}