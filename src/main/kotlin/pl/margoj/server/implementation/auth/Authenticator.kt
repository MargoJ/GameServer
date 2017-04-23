package pl.margoj.server.implementation.auth

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class Authenticator
{
    private val counter = AtomicInteger()
    private val callbacks = ConcurrentHashMap<String, MutableList<(Boolean) -> Unit>>()
    private val executor: Executor = Executors.newCachedThreadPool { Thread(it, "MargoJAuth#${counter.incrementAndGet()}") }

    fun authenticate(aid: String): Boolean
    {
        return true // TODO
    }

    fun authenticateAsync(aid: String, callback: (Boolean) -> Unit)
    {
        val existing = callbacks[aid]
        if (existing != null)
        {
            existing.add(callback)
            return
        }

        executor.execute {
            val list = mutableListOf<(Boolean) -> Unit>()
            list.add(callback)
            callbacks.put(aid, list)
            val result = authenticate(aid)
            callbacks.remove(aid)
            list.forEach { it.invoke(result) }
        }
    }
}