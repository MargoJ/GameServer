package pl.margoj.server.implementation.network.http

interface HttpHandler
{
    fun shouldHandle(path: String): Boolean

    fun handle(request: HttpRequest, response: HttpResponse)
}