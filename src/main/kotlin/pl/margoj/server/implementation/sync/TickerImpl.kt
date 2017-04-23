package pl.margoj.server.implementation.sync

import pl.margoj.server.api.sync.Tickable
import pl.margoj.server.api.sync.Ticker
import pl.margoj.server.implementation.ServerImpl

class TickerImpl(val server: ServerImpl, val mainThread: Thread) : Ticker
{
    private var waitTime = 0L
    private var lastTick = 0L
    private var tickSection = 0L
    private var catchupTime = 0L

    override var currentTick = 0L
        private set

    override val tickables = mutableListOf<Tickable>()
    override val recentTps = DoubleArray(3)

    override var targetTps: Int = 0
        set(value)
        {
            field = value
            waitTime = Ticker.NANOS_IN_SECOND / value
        }

    override val isInMainThread: Boolean
        get() = Thread.currentThread() == this.mainThread

    fun init()
    {
        lastTick = System.nanoTime()
        tickSection = this.lastTick
        catchupTime = 0L
        currentTick = 0L
        recentTps.fill(this.targetTps.toDouble())
    }

    fun tick()
    {
        val curTime = System.nanoTime()
        val wait = this.waitTime - (curTime - lastTick) - catchupTime

        if (wait > 0L)
        {
            Thread.sleep(wait / Ticker.NANOS_IN_MILLI)
            catchupTime = 0L
        }
        else
        {
            catchupTime = minOf(Ticker.NANOS_IN_SECOND, Math.abs(wait))

            if ((this.currentTick++ % 100) == 0L)
            {
                val currentTps = Math.min(targetTps.toDouble(), Ticker.NANOS_IN_SECOND.toDouble() / (curTime - this.tickSection) * 100)
                // idk how it works, and i don't want to know
                recentTps[0] = this.calcTps(recentTps[0], 0.92, currentTps)
                recentTps[1] = this.calcTps(recentTps[1], 0.9835, currentTps)
                recentTps[2] = this.calcTps(recentTps[2], 0.9945000000000001, currentTps)
                tickSection = curTime
            }

            lastTick = currentTick


            tickables.forEach {
                try
                {
                    it.tick(currentTick)
                }
                catch(e: Exception)
                {
                    server.logger.error("Exception while executing task ${it::class.java.name}")
                    e.printStackTrace()
                }
            }
        }
    }

    override fun registerTickable(tickable: Tickable): Boolean
    {
        return this.tickables.add(tickable)
    }

    override fun unregisterTickable(tickable: Tickable): Boolean
    {
        return this.tickables.remove(tickable)
    }

    private fun calcTps(avg: Double, exp: Double, tps: Double): Double
    {
        return avg * exp + tps * (1.0 - exp)
    }
}