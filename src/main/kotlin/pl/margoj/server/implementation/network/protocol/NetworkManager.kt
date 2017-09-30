package pl.margoj.server.implementation.network.protocol

import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.auth.AuthSession
import pl.margoj.server.implementation.player.PlayerConnection
import java.util.concurrent.atomic.AtomicInteger

class NetworkManager(val server: ServerImpl)
{
    private val idsCounter = AtomicInteger()
    private val connections = hashMapOf<String, PlayerConnection>()
    val allConnections: Collection<PlayerConnection> get() = this.connections.values

    fun getHandler(gameToken: String?): PacketHandler?
    {
        return this.connections[gameToken]
    }

    fun createPlayerConnection(authSession: AuthSession): PlayerConnection
    {
        val gameToken = authSession.gameToken
        if (this.connections.containsKey(gameToken))
        {
            throw IllegalStateException("Connection with id $gameToken already exists")
        }
        val connection = PlayerConnection(this, authSession)
        this.connections[gameToken] = connection
        return connection
    }

    fun resetPlayerConnection(connection: PlayerConnection)
    {
        this.connections.values.remove(connection)
    }
}