package pl.margoj.server.implementation.event

import pl.margoj.server.api.event.*
import pl.margoj.server.api.plugin.MargoJPlugin
import pl.margoj.server.implementation.ServerImpl
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.util.TreeSet

class EventManagerImpl(val server: ServerImpl) : EventManager
{
    private val allHandlers = TreeSet<RegisteredHandler>()

    override fun register(owner: MargoJPlugin<*>, listener: EventListener)
    {
        for (method in listener.javaClass.methods)
        {
            val annotation = method.getDeclaredAnnotation(Handler::class.java) ?: continue

            if (method.parameterCount != 1 || !Event::class.java.isAssignableFrom(method.parameterTypes[0]))
            {
                throw IllegalStateException("method $method has invalid signature")
            }

            val methodHandle = MethodHandles.lookup().unreflect(method).bindTo(listener)

            @Suppress("UNCHECKED_CAST")
            this.allHandlers.add(RegisteredHandler(methodHandle, owner, annotation, listener, method.parameterTypes[0] as Class<Event>))
        }
    }

    override fun unregister(listener: EventListener): Boolean
    {
        return this.unregisterByCriteria { it.listener == listener }
    }

    override fun unregisterAll(owner: MargoJPlugin<*>): Boolean
    {
        return this.unregisterByCriteria { it.plugin == owner }
    }

    override fun call(event: Event)
    {
        if (!event.async && !server.ticker.isInMainThread)
        {
            throw IllegalStateException("trying to call a sync event $event from non-main thread")
        }

        for (handler in this.allHandlers)
        {
            if (!handler.eventType.isInstance(event))
            {
                continue
            }

            handler.handle.invokeWithArguments(event)

            if (event is CancellableEvent && event.cancelled)
            {
                return
            }
        }
    }

    private fun unregisterByCriteria(criteria: (RegisteredHandler) -> Boolean): Boolean
    {
        var anyChanged = false
        val iterator = this.allHandlers.iterator()

        while (iterator.hasNext())
        {
            if (criteria(iterator.next()))
            {
                iterator.remove()
                anyChanged = true
            }
        }

        return anyChanged
    }
}

data class RegisteredHandler(val handle: MethodHandle, val plugin: MargoJPlugin<*>, val handlerAnnotation: Handler, val listener: EventListener, val eventType: Class<Event>) : Comparable<RegisteredHandler>
{
    override fun compareTo(other: RegisteredHandler): Int
    {
        return this.handlerAnnotation.prority - other.handlerAnnotation.prority
    }
}