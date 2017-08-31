package pl.margoj.server.implementation

import jline.console.ConsoleReader
import pl.margoj.server.api.sync.Tickable
import java.io.IOException

class ConsoleReaderThread(private val server: ServerImpl, private var useJLine: Boolean) : Thread("MargoJ|ConsoleReaderThread")
{
    override fun run()
    {
        try
        {
            var reader: ConsoleReader? = null

            if (useJLine)
            {
                try
                {
                    reader = ConsoleReader(ORIGINAL_IN, ORIGINAL_OUT)
                }
                catch (ex: Throwable)
                {
                    useJLine = false
                }

            }

            if (!useJLine)
            {
                System.setProperty("jline.terminal", "jline.UnsupportedTerminal")
                reader = ConsoleReader(ORIGINAL_IN, ORIGINAL_OUT)
            }

            while (this.server.running)
            {
                val line = (if (useJLine) reader!!.readLine(">") else reader!!.readLine())?.trim()

                if (line != null && !line.isEmpty())
                {
                    try
                    {
                        this.server.ticker.tickOnce(object : Tickable {
                            override fun tick(currentTick: Long)
                            {
                                server.commandsManager.dispatchCommand(server.consoleCommandSender, line)
                            }
                        })
                    }
                    catch (e: Throwable)
                    {
                        e.printStackTrace()
                    }
                }
            }
        }
        catch (e: IOException)
        {
            e.printStackTrace()
        }
    }
}
