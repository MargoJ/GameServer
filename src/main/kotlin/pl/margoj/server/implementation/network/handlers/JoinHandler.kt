package pl.margoj.server.implementation.network.handlers

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.cookie.ServerCookieEncoder
import io.netty.util.AsciiString
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.network.http.HttpHandler
import pl.margoj.server.implementation.network.http.HttpRequest
import pl.margoj.server.implementation.network.http.HttpResponse
import pl.margoj.server.implementation.utils.GsonUtils

class JoinHandler(private val server: ServerImpl) : HttpHandler
{
    private companion object
    {
        val tokenMatcher = "[a-zA-Z0-9]{255}".toRegex()

        val serverCookieEncoder = ServerCookieEncoder.LAX

        val setCookieHeader = AsciiString("Set-Cookie")

        val cachedOkJson: String

        val cachedInvalidJson: String

        init
        {
            val jsonOk = JsonObject()
            jsonOk.addProperty("ok", true)
            cachedOkJson = jsonOk.toString()

            val jsonInvalid = JsonObject()
            jsonInvalid.addProperty("error", true)
            jsonInvalid.addProperty("message", "Invalid payload")
            cachedInvalidJson = jsonInvalid.toString()
        }
    }

    override fun shouldHandle(path: String): Boolean
    {
        return path == "/joinServer"
    }

    override fun handle(request: HttpRequest, response: HttpResponse)
    {
        if (request.method !== HttpMethod.POST)
        {
            response.responseString = "Invalid method"
            response.status = HttpResponseStatus.METHOD_NOT_ALLOWED
            return
        }

        val json = GsonUtils.parser.parse(request.contentAsString) as? JsonObject

        if (json != null)
        {
            val gameTokenElement = json.get("game_token")

            if (!gameTokenElement.isJsonPrimitive || !(gameTokenElement as JsonPrimitive).isString)
            {
                response.responseString = cachedInvalidJson
            }
            else
            {
                val token = gameTokenElement.asString

                if (!tokenMatcher.matches(token))
                {
                    response.responseString = cachedInvalidJson
                }
                else
                {

                    val header = StringBuilder("margoj_game_token=")
                    header.append(token)
                    header.append("; Expires=Fri, 31 Dec 9999 23:59:59 GMT; HttpOnly");

                    response.headers.put(setCookieHeader, header.toString())
                    response.responseString = cachedOkJson
                }
            }
        }
        else
        {
            response.responseString = cachedInvalidJson
        }

        response.contentType = "application/json"
    }
}