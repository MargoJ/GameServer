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

        this.tasks.put(task.id, task)
    }

    override fun tick(currentTick: Long)
    {
        for (task in tasks.values)
        {
            if (task.cancelled)
            {
                this.tasks.remove(task.id)
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
                    this.tasks.remove(task.id)
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
        this.tasks.values.filter { it.owner === plugin }.forEach { it.cancelled = true }
    }
}