package pl.margoj.server.implementation.network.handlers

import org.apache.commons.io.IOUtils
import pl.margoj.server.implementation.network.http.HttpHandler
import pl.margoj.server.implementation.network.http.HttpRequest
import pl.margoj.server.implementation.network.http.HttpResponse
import java.nio.file.*

class ResourceHandler(private val path: String, private val classLoader: ClassLoader) : HttpHandler
{
    private val cache: MutableMap<String, Pair<String, ByteArray>?> = hashMapOf()
    private var rootPath: Path

    init
    {
        val uri = classLoader.getResource(this.path).toURI()
        var rootPath: Path? = null

        while (rootPath == null)
        {
            try
            {
                rootPath = Paths.get(uri).toAbsolutePath()
            }
            catch(e: FileSystemNotFoundException)
            {
                FileSystems.newFileSystem(uri, hashMapOf(Pair("env", "create")))
            }
        }

        this.rootPath = rootPath
    }

    override fun shouldHandle(path: String): Boolean
    {
        val fixed = fixPath(path)

        if (this.cache.containsKey(path))
        {
            return this.cache[fixed] != null
        }

        try
        {
            val url = this.classLoader.getResource(this.path + fixed)
            val path2 = if (url == null) null else Paths.get(url.toURI())

            if (url == null || path2 == null || !path2.startsWith(this.rootPath))
            {
                this.cache[fixed] = null
                return false
            }

            val stream = url.openStream()

            this.cache[fixed] = Pair(Files.probeContentType(path2), IOUtils.toByteArray(stream))
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