package pl.margoj.server.implementation.network.handlers

import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.network.http.HttpHandler
import pl.margoj.server.implementation.network.http.HttpRequest
import pl.margoj.server.implementation.network.http.HttpResponse
import pl.margoj.server.implementation.network.protocol.OutgoingPacket

class ShutdownHandler(private val server: ServerImpl) : HttpHandler
{
    private val out: String

    init
    {
        val packet = OutgoingPacket()
        packet.addAlert("Serwer jest wyłączany!")
        packet.addEngineAction(OutgoingPacket.EngineAction.STOP)
        out = packet.toString()
    }

    override fun shouldHandle(path: String): Boolean
    {
        return path == "/engine"
    }

    override fun handle(request: HttpRequest, response: HttpResponse)
    {
        response.contentType = "application/json"
        response.responseString = out
    }
}