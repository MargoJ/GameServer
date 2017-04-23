package pl.margoj.server.implementation.network.http

import io.netty.buffer.ByteBuf
import io.netty.handler.codec.http.HttpHeaders
import io.netty.handler.codec.http.HttpMessage
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpUtil

data class HttpRequest(
        val msg: HttpMessage,
        val path: String,
        val uri: String,
        val ipAddress: String,
        val method: HttpMethod,
        val headers: HttpHeaders,
        val queryParameters: Map<String, String>,
        val content: ByteBuf
)
{
    var contentAsString = ""
        private set
        get()
        {
            if (field.isEmpty())
            {
                val buf = content.copy()
                val out = ByteArray(buf.readableBytes())
                buf.readBytes(out)
                val charset = HttpUtil.getCharset(msg)
                field = String(out, charset)
            }
            return field
        }
}