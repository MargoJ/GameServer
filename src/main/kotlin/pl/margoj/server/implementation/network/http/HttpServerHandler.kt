package pl.margoj.server.implementation.network.http

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.*
import io.netty.util.AsciiString
import java.net.InetSocketAddress

class HttpServerHandler(private val server: HttpServer) : ChannelInboundHandlerAdapter()
{
    override fun channelRead(ctx: ChannelHandlerContext, request: Any)
    {
        if (request !is FullHttpRequest)
        {
            return
        }

        if (HttpUtil.is100ContinueExpected(request))
        {
            ctx.write(DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE))
        }

        val ip = (ctx.channel().remoteAddress() as InetSocketAddress).address.hostAddress
        val query = NoSemicolonSingleValueQueryStringDecoder(request.uri())

        val response = HttpResponse(true)
        val delayMetaData = DelayMetaData(response, this, ctx)
        response.delayMetaData = delayMetaData

        server.handle(HttpRequest(request, query.path, request.uri(), ip, request.method(), request.headers(), query.parameters, request.content()), response)

        if(!HttpUtil.isKeepAlive(request))
        {
            response.keepAlive = true
        }

        if(!response.delayed)
        {
            this.sendResponse(response, ctx)
        }
    }

    fun sendResponse(response: HttpResponse, ctx: ChannelHandlerContext)
    {
        val httpResponse = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, response.status, Unpooled.wrappedBuffer(response.response))
        response.headers.forEach { key, value -> httpResponse.headers().set(key ,value) }
        httpResponse.headers()[HttpHeaderNames.CONTENT_LENGTH] = httpResponse.content().readableBytes()

        if(response.keepAlive)
        {
            httpResponse.headers()[HttpHeaderNames.CONNECTION] = KEEP_ALIVE

            ctx.writeAndFlush(httpResponse)
        }
        else
        {
            ctx.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE)
        }
    }

    @Suppress("OverridingDeprecatedMember")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable)
    {
        cause.printStackTrace()
        ctx.close()
    }

    companion object
    {
        private val KEEP_ALIVE = AsciiString("keep-alive")
    }
}