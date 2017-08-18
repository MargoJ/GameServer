package pl.margoj.server.implementation.sync

import pl.margoj.server.api.plugin.MargoJPlugin
import pl.margoj.server.api.sync.TaskBuilder

internal class TaskBuilderImpl(val scheduler: SchedulerImpl, val owner: MargoJPlugin<*>?) : TaskBuilder
{
    private var mode: Task.Mode? = null
    private var action: Runnable? = null
    private var delay: Int? = null
    private var repeat: Int? = null

    override fun sync(): TaskBuilder
    {
        this.mode = Task.Mode.SYNC
        return this
    }

    override fun async(): TaskBuilder
    {
        this.mode = Task.Mode.ASYNC
        return this
    }

    override fun withRunnable(runnable: Runnable): TaskBuilder
    {
        this.action = runnable
        return this
    }

    override fun withRunnable(action: () -> Unit): TaskBuilder
    {
        return this.withRunnable(Runnable { action() })
    }

    override fun delay(ticks: Int): TaskBuilder
    {
        this.delay = ticks
        return this
    }

    override fun delaySeconds(seconds: Int): TaskBuilder
    {
        return this.delay(this.secondsToTicks(seconds))
    }

    override fun repeat(ticks: Int): TaskBuilder
    {
        this.repeat = ticks
        return this
    }

    override fun repeatSeconds(seconds: Int): TaskBuilder
    {
        return this.repeat(this.secondsToTicks(seconds))
    }

    override fun submit(): Int
    {
        val task = Task(
                id = this.scheduler.assignNextId(),
                mode = this.mode!!,
                action = this.action!!,
                delay = this.delay ?: 0,
                needsRepeat = this.repeat != null,
                repeat = this.repeat ?: 0,
                owner = this.owner,
                registrationTick = this.scheduler.server.ticker.currentTick
        )

        this.scheduler.submitTask(task)

        return task.id
    }

    private fun secondsToTicks(seconds: Int): Int
    {
        return this.scheduler.server.ticker.targetTps * seconds
    }
}