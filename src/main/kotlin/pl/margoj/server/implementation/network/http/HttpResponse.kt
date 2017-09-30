package pl.margoj.server.implementation.network.http

import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.util.AsciiString
import java.nio.charset.StandardCharsets

data class HttpResponse(
        var request: HttpRequest,
        var keepAlive: Boolean,
        var response: ByteArray = ByteArray(0),
        var status: HttpResponseStatus = HttpResponseStatus.OK,
        var headers: MutableMap<AsciiString, Any> = hashMapOf(),
        var delayed: Boolean = false
)
{
    var delayMetaData: DelayMetaData? = null

    var responseString: String? = null
        set(value)
        {
            field = value
            response = value!!.toByteArray(StandardCharsets.UTF_8)
        }

    var contentType: String?
        get()
        {
            val header = this.headers[HttpHeaderNames.CONTENT_TYPE]
            return if (header != null && header is String) header else null
        }
        set(value)
        {
            this.headers[HttpHeaderNames.CONTENT_TYPE] = value + "; charset=utf-8"
        }

    fun sendDelayed()
    {
        if (!this.delayed || this.delayMetaData == null)
        {
            throw IllegalStateException("Not delayed")
        }
        this.delayMetaData!!.send()
    }
}