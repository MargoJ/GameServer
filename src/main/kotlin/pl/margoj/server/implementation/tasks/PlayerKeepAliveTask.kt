package pl.margoj.server.implementation.tasks

import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.player.PlayerImpl

class PlayerKeepAliveTask(val server: ServerImpl, keepAliveSeconds: Int) : Runnable
{
    private val keepAlive: Long = keepAliveSeconds.toLong() * 1000L

    override fun run()
    {
        if (this.server.players.isEmpty())
        {
            return
        }

        val iterator = this.server.players.iterator() as MutableIterator<PlayerImpl>

        while (iterator.hasNext())
        {
            val player = iterator.next()

            if (player.connection.lastPacket != 0L && player.connection.lastPacket + keepAlive < System.currentTimeMillis())
            {
                iterator.remove()
                player.disconnect()
                server.logger.debug("Timed out: ${player.name}")
            }
        }
    }
}