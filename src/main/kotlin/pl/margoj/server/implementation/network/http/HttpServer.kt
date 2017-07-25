package pl.margoj.server.implementation.network.http

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.EventLoopGroup
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpResponseStatus
import org.apache.logging.log4j.Logger

class HttpServer(private val logger: Logger, private val host: String, private val port: Int)
{
    private val handlers: MutableList<HttpHandler> = arrayListOf()
    private var bossGroup: EventLoopGroup? = null
    private var workerGroup: EventLoopGroup? = null
    private var channel: Channel? = null
    var filter = "*"

    fun start()
    {
        this.bossGroup = if (Epoll.isAvailable()) EpollEventLoopGroup(1) else NioEventLoopGroup(1)
        this.workerGroup = if (Epoll.isAvailable()) EpollEventLoopGroup() else NioEventLoopGroup()

        this.channel = ServerBootstrap()
                .group(this.bossGroup, this.workerGroup)
                .channel(if (Epoll.isAvailable()) EpollServerSocketChannel::class.java else NioServerSocketChannel::class.java)
                .childHandler(HttpServerInitializer(this))
                .bind(this.host, this.port)
                .syncUninterruptibly()
                .channel()

        val address = this.host + if (this.port != 80) ":$port" else ""
        logger.info("Serwer HTTP uruchomiony na adresie http://$address")
    }

    fun shutdown()
    {
        this.bossGroup?.shutdownGracefully()
        this.workerGroup?.shutdownGracefully()
    }

    fun unregisterAllHandlers()
    {
        this.handlers.clear()
    }

    fun registerHandler(handler: HttpHandler): Boolean
    {
        return this.handlers.add(handler)
    }

    fun unregisterHandler(handler: HttpHandler): Boolean
    {
        return this.handlers.remove(handler)
    }

    internal fun handle(httpRequest: HttpRequest, httpResponse: HttpResponse)
    {
        var foundAny = false
        this.handlers.stream().forEach {
            handler ->
            if (handler.shouldHandle(httpRequest.path))
            {
                handler.handle(httpRequest, httpResponse)
                foundAny = true
                return@forEach
            }
        }

        if (!foundAny)
        {
            httpResponse.responseString = "404 Not Found"
            httpResponse.status = HttpResponseStatus.NOT_FOUND
            httpResponse.contentType = "text/plain"
        }
    }
}