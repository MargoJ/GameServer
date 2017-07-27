package pl.margoj.server.implementation.utils

import io.netty.util.internal.logging.InternalLogLevel
import io.netty.util.internal.logging.InternalLogger
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Logger

class NettyLog4j2Logger(private val logger: Logger) : InternalLogger
{
    override fun name(): String
    {
        return logger.name
    }

    override fun isTraceEnabled(): Boolean
    {
        return logger.isTraceEnabled
    }

    override fun trace(msg: String)
    {
        logger.trace(msg)
    }

    override fun trace(format: String, arg: Any)
    {
        logger.trace(format, arg)
    }

    override fun trace(format: String, argA: Any, argB: Any)
    {
        logger.trace(format, argA, argB)
    }

    override fun trace(format: String, vararg arguments: Any)
    {
        logger.trace(format, *arguments)
    }

    override fun trace(msg: String, t: Throwable)
    {
        logger.trace(msg, t)
    }

    override fun trace(t: Throwable)
    {
        logger.trace(t)
    }

    override fun isDebugEnabled(): Boolean
    {
        return logger.isDebugEnabled
    }

    override fun debug(msg: String)
    {
        logger.debug(msg)
    }

    override fun debug(format: String, arg: Any)
    {
        logger.debug(format, arg)
    }

    override fun debug(format: String, argA: Any, argB: Any)
    {
        logger.debug(format, argA, argB)
    }

    override fun debug(format: String, vararg arguments: Any)
    {
        logger.debug(format, *arguments)
    }

    override fun debug(msg: String, t: Throwable)
    {
        logger.debug(msg, t)
    }

    override fun debug(t: Throwable)
    {
        logger.debug(t)
    }

    override fun isInfoEnabled(): Boolean
    {
        return logger.isInfoEnabled
    }

    override fun info(msg: String)
    {
        logger.info(msg)
    }

    override fun info(format: String, arg: Any)
    {
        logger.info(format, arg)
    }

    override fun info(format: String, argA: Any, argB: Any)
    {
        logger.info(format, argA, argB)
    }

    override fun info(format: String, vararg arguments: Any)
    {
        logger.info(format, *arguments)
    }

    override fun info(msg: String, t: Throwable)
    {
        logger.info(msg, t)
    }

    override fun info(t: Throwable)
    {
        logger.info(t)
    }

    override fun isWarnEnabled(): Boolean
    {
        return logger.isWarnEnabled
    }

    override fun warn(msg: String)
    {
        logger.warn(msg)
    }

    override fun warn(format: String, arg: Any)
    {
        logger.warn(format, arg)
    }

    override fun warn(format: String, vararg arguments: Any)
    {
        logger.warn(format, *arguments)
    }

    override fun warn(format: String, argA: Any, argB: Any)
    {
        logger.warn(format, argA, argB)
    }

    override fun warn(msg: String, t: Throwable)
    {
        logger.warn(msg, t)
    }

    override fun warn(t: Throwable)
    {
        logger.warn(t)
    }

    override fun isErrorEnabled(): Boolean
    {
        return logger.isErrorEnabled
    }

    override fun error(msg: String)
    {
        logger.error(msg)
    }

    override fun error(format: String, arg: Any)
    {
        logger.error(format, arg)
    }

    override fun error(format: String, argA: Any, argB: Any)
    {
        logger.error(format, argA, argB)
    }

    override fun error(format: String, vararg arguments: Any)
    {
        logger.error(format, *arguments)
    }

    override fun error(msg: String, t: Throwable)
    {
        logger.error(msg, t)
    }

    override fun error(t: Throwable)
    {
        logger.error(t)
    }

    override fun isEnabled(level: InternalLogLevel): Boolean
    {
        return logger.isEnabled(fromInternal(level))
    }

    override fun log(level: InternalLogLevel, msg: String)
    {
        logger.log(fromInternal(level), msg)
    }

    override fun log(level: InternalLogLevel, format: String, arg: Any)
    {
        logger.log(fromInternal(level), format, arg)
    }

    override fun log(level: InternalLogLevel, format: String, argA: Any, argB: Any)
    {
        logger.log(fromInternal(level), format, argA, argB)

    }

    override fun log(level: InternalLogLevel, format: String, vararg arguments: Any)
    {
        logger.log(fromInternal(level), format, *arguments)
    }

    override fun log(level: InternalLogLevel, msg: String, t: Throwable)
    {
        logger.log(fromInternal(level), msg, t)

    }

    override fun log(level: InternalLogLevel, t: Throwable)
    {
        logger.log(fromInternal(level), t)
    }

    private fun fromInternal(level: InternalLogLevel): Level?
    {
        when (level)
        {
            InternalLogLevel.TRACE -> return Level.TRACE
            InternalLogLevel.DEBUG -> return Level.DEBUG
            InternalLogLevel.INFO -> return Level.INFO
            InternalLogLevel.WARN -> return Level.WARN
            InternalLogLevel.ERROR -> return Level.ERROR
            else -> return null
        }
    }
}
