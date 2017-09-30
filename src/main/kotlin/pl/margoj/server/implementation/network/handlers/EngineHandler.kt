package pl.margoj.server.implementation.network.handlers

import io.netty.handler.codec.http.cookie.ServerCookieDecoder
import org.apache.commons.lang3.exception.ExceptionUtils
import pl.margoj.server.api.utils.Parse
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.network.http.HttpHandler
import pl.margoj.server.implementation.network.http.HttpRequest
import pl.margoj.server.implementation.network.http.HttpResponse
import pl.margoj.server.implementation.network.http.NoSemicolonSingleValueQueryStringDecoder
import pl.margoj.server.implementation.network.protocol.IncomingPacket
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.network.protocol.PacketHandler
import pl.margoj.server.implementation.player.PlayerConnection

class EngineHandler(private val server: ServerImpl) : HttpHandler
{
    private val cookieDecoder = ServerCookieDecoder.LAX

    override fun shouldHandle(path: String): Boolean
    {
        return path == "/engine"
    }

    override fun handle(request: HttpRequest, response: HttpResponse)
    {
        response.contentType = "application/json"

        val packet = IncomingPacket(
                request.queryParameters["t"] ?: "_",
                request.queryParameters,
                NoSemicolonSingleValueQueryStringDecoder("?" + request.contentAsString).parameters
        )

        var gameToken: String? = null
        val cookiesHeader = request.headers["Cookie"]
        if(cookiesHeader != null)
        {
            val cookies = cookieDecoder.decode(cookiesHeader)
            val tokenCookie = cookies.find { it.name() == "margoj_game_token" }
            if(tokenCookie != null)
            {
                gameToken = tokenCookie.value()
            }
        }

        val handler = this.server.networkManager.getHandler(gameToken)

        val callback: (OutgoingPacket) -> Unit = { out ->
            if (out.shouldStop)
            {
                response.keepAlive = false
            }

            response.responseString = out.toString()
        }

        if (handler == null)
        {
            response.delayed = true

            this.server.authenticator.authenticate(gameToken) { authSession ->
                if (authSession != null)
                {
                    val connection = server.networkManager.createPlayerConnection(authSession)
                    connection.ip = request.ipAddress
                    this.handle(response, connection, packet, OutgoingPacket(), callback)
                }
                else
                {
                    response.responseString = OutgoingPacket().addEngineAction(OutgoingPacket.EngineAction.STOP).addAlert("Błąd autoryzacji. <br> <a href=\"" + this.server.authenticator.authConfig.loginpage + "\">Zaloguj się</a>").toString()
                    response.keepAlive = false
                    response.sendDelayed()
                }
            }
        }
        else
        {
            if (handler is PlayerConnection && handler.ip != request.ipAddress)
            {
                handler.ip = request.ipAddress
            }
            this.handle(response, handler, packet, OutgoingPacket(), callback)
        }
    }

    private fun handle(response: HttpResponse, handler: PacketHandler, packet: IncomingPacket, out: OutgoingPacket, callback: (OutgoingPacket) -> Unit)
    {
        try
        {
            handler.handle(response, packet, out, callback)
        }
        catch (e: Exception)
        {
            server.logger.error("Exception while handling player request, player=${if (handler is PlayerConnection && handler.player != null) handler.player!!.name else "-"}")
            e.printStackTrace()
            handler.disconnect(ExceptionUtils.getStackTrace(e))
        }
    }
}
