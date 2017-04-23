package pl.margoj.server.implementation.utils

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Logger
import pl.margoj.server.api.utils.rtrim
import java.io.OutputStream


class Log4j2OutputStream(private val logger: Logger, private val level: Level) : OutputStream()
{
    override fun write(b: Int)
    {
        print(b.toChar())
    }

    override fun write(b: ByteArray?)
    {
        print(String(b!!))
    }

    override fun write(b: ByteArray?, off: Int, len: Int)
    {
        print(String(b!!, off, len))
    }

    private fun print(string: String)
    {
        val trimmed = string.rtrim()

        if (trimmed.isEmpty())
        {
            return
        }

        synchronized(logger)
        {
            logger.log(level, trimmed)
        }
    }
}