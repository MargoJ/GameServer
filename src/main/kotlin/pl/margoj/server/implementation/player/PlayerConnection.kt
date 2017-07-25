package pl.margoj.server.implementation.player

import org.apache.commons.lang3.exception.ExceptionUtils
import pl.margoj.server.api.sync.Tickable
import pl.margoj.server.implementation.network.http.HttpResponse
import pl.margoj.server.implementation.network.protocol.IncomingPacket
import pl.margoj.server.implementation.network.protocol.NetworkManager
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.network.protocol.PacketHandler
import pl.margoj.server.implementation.player.sublisteners.PlayerPacketSubListener
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class PlayerConnection(val manager: NetworkManager, val aid: Int) : PacketHandler
{
    private val logger = manager.server.logger
    private var disconnectReason: String? = null
    private var disposed: Boolean = false
    internal var subListeners: Collection<PlayerPacketSubListener> = PlayerPacketSubListener.DEFAULTS.map { it(this) }
    internal val packetModifiers = CopyOnWriteArrayList<(OutgoingPacket) -> Unit>()
    internal var lastEvent: Double = 0.0

    var lastPacket: Long = 0
    var ip: String? = null
    var player: PlayerImpl? = null

    init
    {
        logger.trace("New connection object create for aid=$aid")
    }

    override fun handle(response: HttpResponse, packet: IncomingPacket, out: OutgoingPacket, callback: (OutgoingPacket) -> Unit)
    {
        this.lastPacket = System.currentTimeMillis()

        if (this.disposed)
        {
            out.addEngineAction(OutgoingPacket.EngineAction.RELOAD)
            return callback(out)
        }

        if (this.disconnectReason != null)
        {
            out.addEngineAction(OutgoingPacket.EngineAction.STOP)
            out.addWarn(this.disconnectReason!!)
            this.disconnectReason = null
            return callback(out)
        }

        response.delayed = true

        out.player = this.player
        this.manager.server.ticker.tickOnce(ConnectionTickable(this, response, packet, out, callback))
    }

    override fun disconnect(reason: String)
    {
        this.disconnectReason = reason
    }

    fun addModifier(modifier: (OutgoingPacket) -> Unit)
    {
        this.packetModifiers.add(modifier)
    }

    fun dispose()
    {
        this.player = null
        this.disposed = true
    }

    override fun toString(): String
    {
        return "PlayerConnection(aid=$aid, ip=$ip, player=$player)"
    }
}

private class ConnectionTickable(val playerConnection: PlayerConnection, val response: HttpResponse, val packet: IncomingPacket, val out: OutgoingPacket, val callback: (OutgoingPacket) -> Unit) : Tickable
{
    private companion object
    {
        private val counter = AtomicInteger()
        val delayedSenderExecutor: ExecutorService = Executors.newCachedThreadPool {
            val thread = Thread(it, "DelayedSenderThread-${counter.getAndIncrement()}")
            thread.isDaemon = true
            thread
        }
    }

    override fun toString(): String
    {
        return "ConnectionTickable(connection=$playerConnection)"
    }

    private fun processListener(listener: PlayerPacketSubListener): Boolean
    {
        if (listener.onlyOnPlayer && this.playerConnection.player == null)
        {
            return true
        }

        if (listener.onlyWithType != null && packet.type != listener.onlyWithType)
        {
            return true
        }

        try
        {
            return listener.handle(packet, out, packet.queryParams)
        }
        catch (e: Exception)
        {
            playerConnection.manager.server.logger.error("Exception while player packet, listener=$listener, player=${playerConnection.player?.name}")
            e.printStackTrace()
            playerConnection.disconnect(ExceptionUtils.getStackTrace(e))
            return false
        }
    }

    @Suppress("LoopToCallChain")
    override fun tick(currentTick: Long)
    {
        for (subListener in playerConnection.subListeners)
        {
            if (!subListener.async)
            {
                if (!processListener(subListener))
                {
                    break
                }
            }
        }

        delayedSenderExecutor.submit({
            playerConnection.manager.server.ticker.waitForNext()

            for (subListener in playerConnection.subListeners)
            {
                if (subListener.async)
                {
                    if (!processListener(subListener))
                    {
                        break
                    }
                }
            }

            callback(out)
            this.response.sendDelayed()
        })
    }
}
