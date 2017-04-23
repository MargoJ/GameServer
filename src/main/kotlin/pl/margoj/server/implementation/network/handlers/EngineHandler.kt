package pl.margoj.server.implementation.network.handlers

import org.apache.commons.lang3.exception.ExceptionUtils
import pl.margoj.server.api.utils.Parse
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.network.protocol.IncomingPacket
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.network.protocol.PacketHandler
import pl.margoj.server.implementation.player.PlayerConnection
import pl.margoj.server.implementation.network.http.HttpHandler
import pl.margoj.server.implementation.network.http.HttpRequest
import pl.margoj.server.implementation.network.http.HttpResponse
import pl.margoj.server.implementation.network.http.NoSemicolonSingleValueQueryStringDecoder

class EngineHandler(private val server: ServerImpl) : HttpHandler
{
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

        val aid = Parse.parseInt(packet.queryParams["aid"])
        val handler = this.server.networkManager.getHandler(aid)

        if (handler == null)
        {
            response.delayed = true

            this.server.authenticator.authenticateAsync(aid.toString()) {
                success ->
                if (success)
                {
                    val connection = server.networkManager.createPlayerConnection(aid!!)
                    connection.ip = request.ipAddress
                    val out = OutgoingPacket()
                    this.handle(connection, packet, out)

                    if (out.shouldStop)
                    {
                        response.keepAlive = false
                    }

                    response.responseString = out.toString()
                }
                else
                {
                    response.responseString = OutgoingPacket().addEngineAction(OutgoingPacket.EngineAction.STOP).addAlert("Błąd autoryzacji").toString()
                    response.keepAlive = false
                }
                response.sendDelayed()
            }
        }
        else
        {
            val out = OutgoingPacket()
            if(handler is PlayerConnection && handler.ip != request.ipAddress)
            {
                handler.ip = request.ipAddress
            }
            this.handle(handler, packet, out)
            if(out.shouldStop)
            {
                response.keepAlive = false
            }
            response.responseString = out.toString()
        }
    }

    private fun handle(handler: PacketHandler, packet: IncomingPacket, out: OutgoingPacket)
    {
        try
        {
            handler.handle(packet, out)
        }
        catch (e: Exception)
        {
            server.logger.error("Exception while handling player request, player=${if (handler is PlayerConnection && handler.player != null) handler.player!!.name else "-"}")
            e.printStackTrace()
            handler.disconnect(ExceptionUtils.getStackTrace(e))
        }
    }
}
