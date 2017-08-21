package pl.margoj.server.implementation.network.handlers

import io.netty.handler.codec.http.HttpResponseStatus
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.network.http.HttpHandler
import pl.margoj.server.implementation.network.http.HttpRequest
import pl.margoj.server.implementation.network.http.HttpResponse
import java.io.File
import java.net.URLConnection
import java.nio.file.Files

class ItemsHandler(private val server: ServerImpl) : HttpHandler
{
    private val path = "/obrazki/itemy/"

    override fun shouldHandle(path: String): Boolean
    {
        return path.startsWith(this.path)
    }

    override fun handle(request: HttpRequest, response: HttpResponse)
    {
        val file = this.getItemFile(request.path)

        if (file == null)
        {
            response.status = HttpResponseStatus.NOT_FOUND
            response.responseString = "Item image not found"
            response.contentType = "text/plain"
            return
        }

        response.response = Files.readAllBytes(file.toPath())
        response.contentType = URLConnection.guessContentTypeFromName(file.name)
    }

    private fun getItemFile(path: String): File?
    {
        val fileName = path.substring(this.path.length)

        return this.server.items.find { it.imgFileName == fileName }?.imgFileLocation
    }
}
