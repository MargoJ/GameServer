package pl.margoj.server.implementation.network.handlers

import org.apache.commons.io.IOUtils
import pl.margoj.server.implementation.network.http.HttpHandler
import pl.margoj.server.implementation.network.http.HttpRequest
import pl.margoj.server.implementation.network.http.HttpResponse
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files

class ResourceHandler(val file: File) : HttpHandler
{
    private val cache: MutableMap<String, Pair<String, ByteArray>?> = hashMapOf()

    override fun shouldHandle(path: String): Boolean
    {
        val fixed = fixPath(path)

        if (this.cache.containsKey(path))
        {
            return this.cache[fixed] != null
        }

        try
        {
            val file = File(this.file, fixed)

            if (!file.exists() || file.isDirectory || !file.absoluteFile.startsWith(this.file.absolutePath))
            {
                this.cache[fixed] = null
                return false
            }


            FileInputStream(file).use {
                this.cache[fixed] = Pair(Files.probeContentType(file.toPath()), IOUtils.toByteArray(it))
            }

            return true
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            return false
        }
    }

    override fun handle(request: HttpRequest, response: HttpResponse)
    {
        val path = fixPath(request.path)
        val (contentType, bytes) = cache[path]!!
        response.contentType = contentType
        response.response = bytes
    }

    private fun fixPath(path: String): String
    {
        var fixed = path.trim()
        if (fixed.isEmpty() || fixed == "/")
        {
            fixed = "/index.html"
        }
        return fixed
    }
}