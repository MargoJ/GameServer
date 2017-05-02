package pl.margoj.server.implementation.network.handlers

import pl.margoj.mrf.MargoResource
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.network.http.HttpHandler
import pl.margoj.server.implementation.network.http.HttpRequest
import pl.margoj.server.implementation.network.http.HttpResponse
import java.nio.file.Files

class TownHandler(private val server: ServerImpl) : HttpHandler
{
    private val path = "/obrazki/miasta/"

    override fun shouldHandle(path: String): Boolean
    {
        if (!path.startsWith(this.path))
        {
            return false
        }

        val mapId = this.extractMapId(path) ?: return false

        return this.server.getTownById(mapId) != null
    }

    override fun handle(request: HttpRequest, response: HttpResponse)
    {
        response.response = Files.readAllBytes(this.server.getTownById(this.extractMapId(request.path)!!)!!.image.toPath())
        response.contentType = "image/png"
    }

    private fun extractMapId(path: String): String?
    {
        val fileName = path.substring(this.path.length)

        if (!fileName.endsWith(".png"))
        {
            return null
        }

        val id = fileName.substring(0, fileName.length - "png".length - 1)

        if (!MargoResource.ID_PATTERN.matcher(id).matches())
        {
            return null
        }

        return id
    }
}
