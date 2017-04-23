package pl.margoj.server.implementation.network.http

import io.netty.channel.ChannelHandlerContext

data class DelayMetaData(
        val response: HttpResponse,
        val handler: HttpServerHandler,
        val ctx: ChannelHandlerContext
)
{
    fun send()
    {
        this.handler.sendResponse(this.response, this.ctx)
    }
}