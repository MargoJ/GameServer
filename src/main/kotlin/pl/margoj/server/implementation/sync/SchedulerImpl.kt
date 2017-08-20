package pl.margoj.server.implementation.sync

import pl.margoj.server.api.plugin.MargoJPlugin
import pl.margoj.server.api.sync.Scheduler
import pl.margoj.server.api.sync.TaskBuilder
import pl.margoj.server.api.sync.Tickable
import pl.margoj.server.implementation.ServerImpl
import java.util.Collections
import java.util.LinkedList
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class SchedulerImpl(val server: ServerImpl) : Scheduler, Tickable
{
    private companion object
    {
        val asyncTaskCounter = AtomicInteger()
        val executor = Executors.newCachedThreadPool {
            Thread(it, "MargoJAsyncTask-${asyncTaskCounter.getAndIncrement()}")
        }
    }

    private val tasks: MutableMap<Int, Task> = Collections.synchronizedMap(HashMap())
    private val taskId = AtomicInteger()
    private var processing = false
    private val news = LinkedList<Task>()
    private val deletes = LinkedList<Int>()

    fun start()
    {
        this.server.ticker.registerTickable(this)
    }

    fun assignNextId(): Int
    {
        return this.taskId.getAndIncrement()
    }

    fun submitTask(task: Task)
    {
        if (this.tasks.containsKey(task.id))
        {
            throw IllegalArgumentException("Task ID ${task.id} already taken")
        }

        if (this.processing)
        {
            this.news.add(task)
        }
        else
        {
            this.tasks.put(task.id, task)
        }
    }

    override fun tick(currentTick: Long)
    {
        this.processing = true
        for (task in tasks.values)
        {
            if (task.cancelled)
            {
                this.deletes.add(task.id)
                continue
            }

            if (currentTick < task.registrationTick + task.delay.toLong())
            {
                continue
            }

            if (!task.needsRepeat)
            {
                if (task.lastExecution != -1L) // was already executed
                {
                    this.deletes.add(task.id)
                    continue
                }
            }
            else
            {
                if (task.lastExecution != -1L)
                {
                    if (currentTick < task.lastExecution + task.repeat)
                    {
                        continue
                    }
                }
            }

            task.lastExecution = currentTick

            when (task.mode)
            {
                Task.Mode.SYNC -> task.action.run()
                Task.Mode.ASYNC -> executor.execute(task.action)
            }
        }
        this.processing = false

        this.iterateAndClear(this.deletes) { this.tasks.remove(it) }
        this.iterateAndClear(this.news) { this.tasks.put(it.id, it) }
    }

    private inline fun <T> iterateAndClear(iterable: MutableIterable<T>, consumer: (T) -> Unit)
    {
        val iterator = iterable.iterator()

        while(iterator.hasNext())
        {
            consumer(iterator.next())
            iterator.remove()
        }
    }

    fun systemTask(): TaskBuilder
    {
        return TaskBuilderImpl(this, null)
    }

    override fun task(plugin: MargoJPlugin<*>): TaskBuilder
    {
        return TaskBuilderImpl(this, plugin)
    }

    override fun cancelTask(id: Int)
    {
        val task = this.tasks[id] ?: throw IllegalArgumentException("couldn't find task $id")
        task.cancelled = true
    }

    override fun cancelAll(plugin: MargoJPlugin<*>)
    {
        this.tasks.values.stream().filter { it.owner === plugin }.forEach { it.cancelled = true }
    }
}