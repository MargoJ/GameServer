package pl.margoj.server.implementation.network.http

import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec

internal class HttpServerInitializer(private val server: HttpServer) : ChannelInitializer<Channel>()
{
    override fun initChannel(ch: Channel)
    {
        val p = ch.pipeline()

        p.addLast(HttpServerCodec())
        p.addLast(HttpObjectAggregator(8192))
        p.addLast(HttpServerHandler(this.server))
    }
}