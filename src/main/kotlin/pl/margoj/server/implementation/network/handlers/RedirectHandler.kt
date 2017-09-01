package pl.margoj.server.implementation.network.handlers

import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.network.http.HttpHandler
import pl.margoj.server.implementation.network.http.HttpRequest
import pl.margoj.server.implementation.network.http.HttpResponse

class RedirectHandler(private val server: ServerImpl) : HttpHandler
{
    override fun shouldHandle(path: String): Boolean
    {
        return path == "index.html" || path == "/index.html" || path == "/" || path == ""
    }

    override fun handle(request: HttpRequest, response: HttpResponse)
    {
        response.contentType = "text/plain"

        val out = StringBuilder()
                    .append("<script type=\"application/javascript\">")
                    .append("location.href =\"http://game1.margonem.pl/\"")
                    .append("</script>")

        response.contentType = "text/html"
        response.responseString = out.toString()
    }
}