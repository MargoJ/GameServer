package pl.margoj.server.implementation.network.handlers

import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.util.AsciiString
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.network.http.HttpHandler
import pl.margoj.server.implementation.network.http.HttpRequest
import pl.margoj.server.implementation.network.http.HttpResponse

class DebugHandler(private val server: ServerImpl) : HttpHandler
{
    override fun shouldHandle(path: String): Boolean
    {
        return path.startsWith("/debug/")
    }

    override fun handle(request: HttpRequest, response: HttpResponse)
    {
        response.contentType = "text/plain"

        when (request.path)
        {
            "/debug/version" ->
            {
                response.responseString = "GameServer v" + this.server.version
            }
            "/debug/request" ->
            {
                val out = StringBuilder()
                out.append("Method: ").append(request.method).append("\n")
                out.append("URI: ").append(request.uri).append("\n")
                out.append("Path: ").append(request.path).append("\n")
                out.append("Query Parameters: \n")

                if (request.queryParameters.isEmpty())
                {
                    out.append("\t NONE")
                }
                else
                {
                    request.queryParameters.forEach{
                        key, value ->
                        out.append("\t ").append(key).append(" = ").append(value).append("\n")
                    }
                }
                out.append("\n\n")
                out.append("Headers: \n")

                if (request.headers.isEmpty)
                {
                    out.append("\t NONE")
                }
                else
                {
                    request.headers.forEach {
                        entry ->
                        out.append("\t ").append(entry.key).append(" = ").append(entry.value).append("\n")
                    }
                }
                out.append("\n\n")
                out.append("Request body:\n").append(request.contentAsString)

                response.responseString = out.toString()
            }
            "/debug/sendheaders" ->
            {
                if(request.queryParameters.isEmpty())
                {
                    response.responseString = "No headers requested"
                    response.status = HttpResponseStatus.NOT_ACCEPTABLE
                    return
                }

                request.queryParameters.forEach {
                    (key, value) ->
                    response.headers[AsciiString(key)] = value
                }

                response.responseString = "Sent ${request.queryParameters.size} requested headers"
            }
            else ->
            {
                response.responseString = "Unknown response"
                response.status = HttpResponseStatus.NOT_FOUND
            }
        }
    }
}