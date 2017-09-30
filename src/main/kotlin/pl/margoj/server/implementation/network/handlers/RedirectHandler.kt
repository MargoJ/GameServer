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
        response.contentType = "text/html"

        // TODO
        val out = StringBuilder()
                .append("<div class='__mj_connect_to' data-name='" + this.server.config.serverConfig!!.name + "' data-connect='" + this.server.authenticator.authConfig.server + "'></div>")
                .append("Serwer do autoryzacji używa wtyczki MargoJ<br>")
                .append("Możesz ją pobrać pod <a href=\"https://margoj.pl/extension/download.zip\">tym linkiem</a><br>")
                .append("<br>")
                .append("Jeśli posiadasz wtyczke i nie zostałeś połączony automatycznie ")
                .append("połącz się za pomocą wtyczki do: <b>").append(this.server.authenticator.authConfig.server).append("</b>")

        response.contentType = "text/html"
        response.responseString = out.toString()
    }
}