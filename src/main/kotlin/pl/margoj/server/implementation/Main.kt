package pl.margoj.server.implementation

import io.netty.util.internal.logging.InternalLoggerFactory
import joptsimple.OptionParser
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Logger
import org.yaml.snakeyaml.Yaml
import pl.margoj.server.implementation.utils.Log4j2OutputStream
import pl.margoj.server.implementation.utils.NettyLoggerFactory
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.PrintStream
import java.util.Arrays
import java.util.Collections

val ORIGINAL_IN = System.`in`!!
val ORIGINAL_OUT = System.out!!

fun main(args: Array<String>)
{
    val logger = LogManager.getLogger("MargoJ") as Logger
    val nettyLogger = LogManager.getLogger("Netty") as Logger

    logger.trace("Inicjalizuje logi")
    System.setOut(PrintStream(Log4j2OutputStream(logger, Level.INFO)))
    System.setErr(PrintStream(Log4j2OutputStream(logger, Level.ERROR)))
    InternalLoggerFactory.setDefaultFactory(NettyLoggerFactory(nettyLogger))

    logger.trace("Parsowanie argumentów")

    val optionParser = OptionParser()
    optionParser.acceptsAll(Collections.singletonList("nojline"), "Wylacza JLine jako domylsnie wejscie")
    optionParser.acceptsAll(Arrays.asList("logger", "l")).withRequiredArg().ofType(Level::class.java).describedAs("Logging level")
    optionParser.acceptsAll(Arrays.asList("nettylogger", "n")).withRequiredArg().ofType(Level::class.java).describedAs("Netty logging level")
    optionParser.acceptsAll(Arrays.asList("help", "?"), "Pokazuje ta strone pomocy")
    optionParser.acceptsAll(Arrays.asList("debug", "d"), "Wlacza tryb debuggowania")

    val options = optionParser.parse(*args)

    if (options.has("?"))
    {
        optionParser.printHelpOn(System.out)
        return
    }


    val useJLine = !options.has("nojline")
    val debug = options.has("debug")

    if (debug)
    {
        logger.level = Level.DEBUG
        logger.debug("Debuggownie jest wlaczone")
    }
    if (options.has("logger"))
    {
        logger.level = options.valueOf("logger") as Level
    }
    if (options.has("nettylogger"))
    {
        nettyLogger.level = options.valueOf("nettylogger") as Level
    }

    val file = File("margoj.yml")

    if (!file.exists())
    {
        try
        {
            FileUtils.copyURLToFile(ServerImpl::class.java.classLoader.getResource("margoj.yml"), file)
        }
        catch (e: IOException)
        {
            e.printStackTrace()
            return
        }
    }

    val config = Yaml().loadAs(FileReader(file), MargoJConfigImpl::class.java)

    println(config)

    val server = ServerImpl(config, logger)
    server.useJLine = useJLine
    server.debugModeEnabled = debug
    server.start()
}