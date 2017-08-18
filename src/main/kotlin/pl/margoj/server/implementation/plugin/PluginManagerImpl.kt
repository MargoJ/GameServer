package pl.margoj.server.implementation.plugin

import pl.margoj.server.api.events.plugin.PluginDisableEvent
import pl.margoj.server.api.events.plugin.PluginEnableEvent
import pl.margoj.server.api.events.plugin.PluginInitEvent
import pl.margoj.server.api.plugin.MargoJPlugin
import pl.margoj.server.api.plugin.PluginLoader
import pl.margoj.server.api.plugin.PluginManager
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.plugin.jar.JarPluginLoader
import java.io.File

class PluginManagerImpl(override val server: ServerImpl) : PluginManager
{
    override val pluginLoaders: MutableList<PluginLoader> = arrayListOf(
            JarPluginLoader(this)
    )

    private var plugins_ = HashMap<String, MargoJPlugin<*>>()
    override val plugins: Collection<MargoJPlugin<*>> get() = this.plugins_.values

    override fun load(file: File): MargoJPlugin<*>
    {
        val loader = this.pluginLoaders.filter { it.canLoad(file) }.lastOrNull() ?: throw IllegalArgumentException("no capable loader found")
        val plugin = loader.load(file)
        this.register(plugin)
        return plugin
    }

    override fun loadOne(file: File): MargoJPlugin<*>
    {
        val plugin = this.load(file)
        this.init(plugin)
        return plugin
    }

    override fun loadAll(directory: File): Collection<MargoJPlugin<*>>
    {
        if (!directory.isDirectory)
        {
            throw IllegalArgumentException("not a directory")
        }

        val fileList = directory.listFiles()
        val output = ArrayList<MargoJPlugin<*>>(fileList.size)

        for (file in fileList)
        {
            output.add(this.load(file))
        }

        for (plugin in output)
        {
            this.init(plugin)
        }

        return output
    }

    override fun register(plugin: MargoJPlugin<*>)
    {
        val name = plugin.annotation.name

        if (this.plugins_.containsKey(name))
        {
            throw IllegalStateException("Plugin with name $name already exists!")
        }

        this.plugins_.put(name, plugin)
    }

    override fun init(plugin: MargoJPlugin<*>)
    {
        if (plugin.enabled)
        {
            throw IllegalStateException("Plugin $plugin already enabled")
        }


        val event = PluginInitEvent(plugin)
        this.server.eventManager.call(event)

        if (event.cancelled)
        {
            return
        }

        plugin.load()
        plugin.enabled = true

        this.server.eventManager.call(PluginEnableEvent(plugin))
    }

    override fun disable(plugin: MargoJPlugin<*>)
    {
        if (!plugin.enabled)
        {
            throw IllegalStateException("Plugin $plugin not enabled")
        }

        this.server.eventManager.unregisterAll(plugin)
        this.server.commandsManager.unregisterAll(plugin)
        this.server.scheduler.cancelAll(plugin)

        plugin.unload()
        plugin.enabled = false

        this.server.eventManager.call(PluginDisableEvent(plugin))
    }
}