package pl.margoj.server.implementation.utils

import io.netty.util.internal.logging.InternalLogger
import io.netty.util.internal.logging.InternalLoggerFactory
import org.apache.logging.log4j.Logger

class NettyLoggerFactory(private val logger: Logger) : InternalLoggerFactory()
{
    override fun newInstance(name: String): InternalLogger
    {
        return NettyLog4j2Logger(this.logger)
    }
}