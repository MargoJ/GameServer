package pl.margoj.server.implementation.sync

import pl.margoj.server.api.plugin.MargoJPlugin

class Task(val id: Int, val mode: Mode, val action: Runnable, val delay: Int, val repeat: Int, val needsRepeat: Boolean, val owner: MargoJPlugin<*>?, val registrationTick: Long)
{
    var cancelled = false
    var lastExecution = -1L

    enum class Mode
    {
        SYNC, ASYNC
    }
}