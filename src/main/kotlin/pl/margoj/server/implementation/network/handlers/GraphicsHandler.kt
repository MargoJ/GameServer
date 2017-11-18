package pl.margoj.server.implementation.network.handlers

import io.netty.handler.codec.http.HttpResponseStatus
import pl.margoj.mrf.graphics.GraphicResource
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.network.http.HttpHandler
import pl.margoj.server.implementation.network.http.HttpRequest
import pl.margoj.server.implementation.network.http.HttpResponse
import java.io.File
import java.net.URLConnection
import java.nio.file.Files

class GraphicsHandler(private val server: ServerImpl, directory: File, val category: GraphicResource.GraphicCategory, val path: String) : HttpHandler
{
    val directory = File(directory, category.id.toString())
    val custom = HashMap<String, ByteArray>()

    init
    {
        if (!directory.exists())
        {
            directory.mkdir()
        }
    }

    override fun shouldHandle(path: String): Boolean
    {
        return path.startsWith(this.path)
    }

    override fun handle(request: HttpRequest, response: HttpResponse)
    {
        val custom = this.custom[request.path.substring(this.path.length)]
        if (custom != null)
        {
            response.response = custom
            response.contentType = URLConnection.guessContentTypeFromName(request.path)
            return
        }

        val file = this.getItemFile(request.path)

        if (file == null || !file.exists() || file.isDirectory)
        {
            response.status = HttpResponseStatus.NOT_FOUND
            response.responseString = "Image not found"
            response.contentType = "text/plain"
            return
        }

        response.response = Files.readAllBytes(file.toPath())
        response.contentType = URLConnection.guessContentTypeFromName(file.name)
    }

    private fun getItemFile(path: String): File?
    {
        val fileName = path.substring(this.path.length)
        val file = File(this.directory, fileName)

        if (!file.absolutePath.startsWith(this.directory.absolutePath))
        {
            return null
        }

        return file
    }
}
