package pl.margoj.server.implementation.network.protocol

import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.player.PlayerConnection

class NetworkManager(val server: ServerImpl)
{
    private val connections = hashMapOf<Int, PlayerConnection>()
    private val noAuth = NoAuthPacketHandler(this)

    fun getHandler(aid: Int?): PacketHandler?
    {
        return if (aid == null) this.noAuth else this.connections[aid]
    }

    fun createPlayerConnection(aid: Int): PlayerConnection
    {
        if (this.connections.containsKey(aid))
        {
            throw IllegalStateException("Connection with id $aid already exists")
        }
        val connection = PlayerConnection(this, aid)
        this.connections[aid] = connection
        return connection
    }

    fun resetPlayerConnection(connection: PlayerConnection)
    {
        this.connections.values.remove(connection)
    }
}