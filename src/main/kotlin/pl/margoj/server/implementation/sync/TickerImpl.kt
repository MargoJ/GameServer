package pl.margoj.server.implementation.sync

import pl.margoj.server.api.sync.Tickable
import pl.margoj.server.api.sync.Ticker
import pl.margoj.server.api.sync.Waitable
import pl.margoj.server.implementation.ServerImpl
import java.util.LinkedList
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class TickerImpl(val server: ServerImpl, var mainThread: Thread?) : Ticker
{
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    private val registerQueue = LinkedList<Tickable>()
    private val unregisterQueue = LinkedList<Tickable>()
    private val iteratingNow = AtomicBoolean()
    private var waitTime = 0L
    private var lastTick = 0L
    private var tickSection = 0L
    private var catchupTime = 0L
    private var currentIterator: MutableIterator<Tickable>? = null
    private val asyncCounter = AtomicInteger()
    private val asyncRunner = Executors.newCachedThreadPool { Thread(it, "MargoJ-Async-${asyncCounter.incrementAndGet()}") }

    override var currentTick = 0L
        private set

    override val tickables = LinkedList<Tickable>()
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

            lastTick = curTime

            var registerElement: Tickable?
            var unregisterElement: Tickable?

            while (true)
            {
                registerElement = this.registerQueue.poll()
                unregisterElement = this.unregisterQueue.poll()

                var any = false
                if (registerElement != null)
                {
                    this.tickables.add(registerElement)
                    any = true
                }
                if (unregisterElement != null)
                {
                    this.tickables.remove(unregisterElement)
                    any = true
                }

                if(!any)
                {
                    break
                }
            }

            this.iteratingNow.set(true)
            this.currentIterator = this.tickables.iterator()

            while (this.currentIterator!!.hasNext())
            {
                val tickable = this.currentIterator!!.next()

                val realTickable = (tickable as? OneTimeTickable)?.parent ?: tickable

                try
                {
                    tickable.tick(currentTick)
                }
                catch(e: Exception)
                {
                    server.logger.error("Exception while executing task ${realTickable::class.java.name} ($realTickable)")
                    e.printStackTrace()
                }
            }

            this.currentIterator = null
            this.iteratingNow.set(false)
        }

        lock.withLock {
            condition.signalAll()
        }
    }


    override fun registerTickable(tickable: Tickable)
    {
        if (this.iteratingNow.get())
        {
            this.registerQueue.add(tickable)
        }
        else
        {
            this.tickables.add(tickable)
        }
    }

    override fun unregisterTickable(tickable: Tickable): Boolean
    {
        if (this.iteratingNow.get())
        {
            if(!this.tickables.contains(tickable))
            {
                return false
            }

            this.unregisterQueue.add(tickable)
            return true
        }
        else
        {
            return this.tickables.remove(tickable)
        }
    }

    override fun tickOnce(tickable: Tickable)
    {
        this.registerTickable(OneTimeTickable(tickable))
    }

    override fun <T> registerWaitable(runnable: () -> T): Waitable<T>
    {
        val waitable = WaitableImpl(runnable)
        this.tickOnce(waitable)
        return waitable
    }

    override fun runAsync(runnable: Runnable)
    {
        asyncRunner.submit(runnable)
    }

    private fun calcTps(avg: Double, exp: Double, tps: Double): Double
    {
        return avg * exp + tps * (1.0 - exp)
    }

    fun waitForNext()
    {
        val required = this.currentTick + 2L

        while (required > this.currentTick)
        {
            lock.withLock {
                condition.await()
            }
        }
    }

    fun stop()
    {
        this.currentTick = Long.MAX_VALUE
        lock.withLock {
            condition.signalAll()
        }
    }

    private inner class OneTimeTickable(val parent: Tickable) : Tickable
    {
        override fun tick(currentTick: Long)
        {
            try
            {
                parent.tick(currentTick)
            }
            finally
            {
                this@TickerImpl.currentIterator!!.remove()
            }
        }
    }
}
