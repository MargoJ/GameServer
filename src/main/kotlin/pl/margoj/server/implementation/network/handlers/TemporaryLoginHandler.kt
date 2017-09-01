package pl.margoj.server.implementation.network.handlers

import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.network.http.HttpHandler
import pl.margoj.server.implementation.network.http.HttpRequest
import pl.margoj.server.implementation.network.http.HttpResponse

class TemporaryLoginHandler(val server: ServerImpl) : HttpHandler
{
    private val accounts = HashMap<Long, String>()

    init
    {
        server.databaseManager.withConnectionUnsafe {
            it.createStatement().use {
                it.executeQuery("SELECT `id`, `characterName` FROM `players`").use {
                    while (it.next())
                    {
                        accounts.put(it.getLong("id"), it.getString("characterName"))
                    }
                }
            }
        }
    }

    override fun shouldHandle(path: String): Boolean
    {
        return path == "/login" || path == "/logout"
    }

    override fun handle(request: HttpRequest, response: HttpResponse)
    {
        val out = StringBuilder()

        if(request.path == "/logout")
        {
            out
                    .append("<script type=\"application/javascript\">")
                    .append("document.cookie = \"user_id=; expires=Thu, 01 Jan 1970 00:00:01 GMT;\";")
                    .append("location.href =\"index.html\"")
                    .append("</script>")
        }
        else
        {
            out
                    .append("<script type=\"application/javascript\">")
                    .append("function loginAs(id) {")
                    .append("document.cookie = \"user_id=\" + id;")
                    .append("location.href = \"index.html\";")
                    .append("}")
                    .append("</script>")

            out.append("Zaloguj się jako: <br>")

            for ((id, name) in this.accounts)
            {
                out.append("<a href=\"#\" onclick=\"loginAs(").append(id).append(")\">").append(name).append("</a><br>")
            }
        }

        response.contentType = "text/html"
        response.responseString = out.toString()
    }
}