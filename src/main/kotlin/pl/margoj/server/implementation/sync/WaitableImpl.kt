package pl.margoj.server.implementation.sync

import pl.margoj.server.api.sync.Tickable
import pl.margoj.server.api.sync.Waitable
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class WaitableImpl<out ResultT>(private val runnable: () -> ResultT) : Waitable<ResultT>, Tickable
{
    private var lock: Lock = ReentrantLock()
    private var condition = this.lock.newCondition()
    private var done: Boolean = false
    private var result_: ResultT? = null

    override fun tick(currentTick: Long)
    {
        if (this.done)
        {
            throw IllegalStateException("Already done")
        }

        this.lock.withLock {
            this.result_ = this.runnable()
            this.done = true
            this.condition.signalAll()
        }
    }

    override val isDone: Boolean
        get()
        {
            this.lock.withLock {
                return this.done
            }
        }

   override val result: ResultT?
        get()
        {
            this.lock.withLock {
                if (!this.done)
                {
                    throw IllegalStateException("waitable not done yet")
                }

                return this.result_
            }
        }

    override fun wait(): ResultT?
    {
        while (true)
        {
            try
            {
                this.lock.lock()

                if (this.done)
                {
                    break
                }

                this.condition.await()
            }
            finally
            {
                this.lock.unlock()
            }
        }

        return this.result
    }
}