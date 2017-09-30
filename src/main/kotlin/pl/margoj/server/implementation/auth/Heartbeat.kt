package pl.margoj.server.implementation.auth

import com.google.gson.annotations.SerializedName
import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException
import pl.margoj.server.implementation.player.PlayerConnection
import pl.margoj.server.implementation.player.PlayerImpl
import pl.margoj.server.implementation.utils.GsonUtils

class Heartbeat(val authenticator: Authenticator) : Runnable
{
    private var task: Int = -1
    private var heartbeatQueue = HashMap<PlayerConnection, PlayerImpl?>()

    fun init()
    {
        this.task = this.authenticator.server.scheduler
                .systemTask()
                .withRunnable(this)
                .async()
                .delaySeconds(10.0)
                .repeatSeconds(10.0)
                .submit()
    }

    fun shutdown()
    {
        this.authenticator.server.scheduler.cancelTask(this.task)

        this.run()
    }

    fun queue(connection: PlayerConnection)
    {
        this.heartbeatQueue.put(connection, connection.player)
    }

    override fun run()
    {
        val activeConnections = this.authenticator.server.networkManager.allConnections
        val queueCopy = HashMap(this.heartbeatQueue)
        this.heartbeatQueue.clear()

        val connections = HashSet<PlayerConnection>(activeConnections.size + heartbeatQueue.size)
        connections.addAll(queueCopy.keys)
        connections.addAll(activeConnections)

        if (connections.isEmpty())
        {
            return
        }

        val sessions = HashMap<String, SessionData>(connections.size)

        for (connection in connections)
        {
            val data = SessionData()

            val player = connection.player ?: queueCopy[connection]

            if(player != null)
            {
                data.level = player.level
            }

            sessions.put(connection.authSession.sessionId.toString(), data)
        }

        val payload = HeartbeatPayload(this.authenticator.authConfig.secret, sessions)

        var response: HeartbeatResponse?

        try
        {
            val string = Unirest
                    .post(this.authenticator.authConfig.authserver + "/server/" + this.authenticator.authConfig.serverid + "/heartbeat")
                    .header("content-type", "application/json")
                    .body(GsonUtils.gson.toJson(payload))
                    .asString()

            response = GsonUtils.gson.fromJson(string.body, HeartbeatResponse::class.java)
        }
        catch (e: UnirestException)
        {
            response = null
            e.printStackTrace()
        }

        if(response == null || response.ok != true)
        {
            this.authenticator.server.logger.error("Heartbeat failed: $response")
            for (connection in connections)
            {
                connection.authSession.invalidated = true
            }
            return
        }

        val expiredIds = response.expiredIds!!

        for (connection in this.authenticator.server.networkManager.allConnections)
        {
            if(expiredIds.contains(connection.authSession.sessionId))
            {
                this.authenticator.server.logger.debug("Heartbeat invalidated ${connection.aid}")
                connection.authSession.invalidated = true
            }
        }

        this.authenticator.server.logger.info("Heartbeat successful")
    }

    private data class HeartbeatPayload
    (
            var secret: String,

            var sessions: Map<String, SessionData>
    )

    private data class SessionData
    (
            var level: Int? = null
    )

    private data class HeartbeatResponse @JvmOverloads constructor
    (
            var ok: Boolean? = null,

            @SerializedName("expired_ids")
            var expiredIds: Array<Long>? = null
    )
}