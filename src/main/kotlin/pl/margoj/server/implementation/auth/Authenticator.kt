package pl.margoj.server.implementation.auth

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException
import pl.margoj.server.api.player.PlayerRank
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.utils.GsonUtils
import java.util.concurrent.atomic.AtomicInteger

class Authenticator(val server: ServerImpl, val authConfig: AuthConfig)
{
    private val counter = AtomicInteger()
    val heartbeat = Heartbeat(this)

    fun init()
    {
        this.heartbeat.init()
    }

    fun shutdown()
    {
        this.heartbeat.shutdown()
    }

    fun authenticate(gameToken: String?, callback: (AuthSession?) -> Unit)
    {
        if (gameToken == null)
        {
            callback(null)
            return
        }

        var json: JsonObject?
        try
        {
            json = GsonUtils.parser.parse(
                    Unirest.get(this.authConfig.authserver + "/server/" + this.authConfig.serverid + "/hasJoined")
                            .queryString("game_token", gameToken)
                            .queryString("server_secret", this.authConfig.secret)
                            .asString().body
            ) as? JsonObject
        }
        catch (e: UnirestException)
        {
            e.printStackTrace()
            json = null
        }

        if (json == null)
        {
            this.server.gameLogger.error("Błąd komunikacji z serwerem autoryzacji")
            callback(null)
            return
        }

        val ok = json["ok"]
        if (ok !is JsonPrimitive || !ok.isBoolean || !ok.asBoolean)
        {
            this.server.logger.warn("Błąd logowania: " + json["exception_localized_message"].asString)
            callback(null)
            return
        }

        val character = json["character"] as JsonObject

        val authSession = AuthSession(
                gameToken = gameToken,
                accountId = character["account_id"].asLong,
                charId = character["id"].asLong,
                sessionId = json["current_session_id"].asLong,
                charName = character["name"].asString,
                charProfession = character["profession"].asString,
                charGender = character["gender"].asString,
                rank = PlayerRank.valueOf(character["role"].asString)
        )

        callback(authSession)
    }
}