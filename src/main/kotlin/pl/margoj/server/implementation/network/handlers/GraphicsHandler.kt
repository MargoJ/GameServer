package pl.margoj.server.implementation.network.handlers

import io.netty.handler.codec.http.HttpResponseStatus
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.network.http.HttpHandler
import pl.margoj.server.implementation.network.http.HttpRequest
import pl.margoj.server.implementation.network.http.HttpResponse
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URLConnection
import java.nio.file.Files
import javax.imageio.ImageIO

class GraphicsHandler(private val server: ServerImpl, private val directory: File) : HttpHandler
{
    private val custom = HashMap<String, ByteArray>()
    private val path = "/obrazki/npc/"

    init
    {
        val transparent = BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB)
        val bytes = ByteArrayOutputStream()
        ImageIO.write(transparent, "png", bytes)
        this.custom.put("reserved/transparent.png", bytes.toByteArray())
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
