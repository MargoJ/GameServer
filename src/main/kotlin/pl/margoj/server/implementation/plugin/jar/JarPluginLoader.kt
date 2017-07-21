package pl.margoj.server.implementation.plugin.jar

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Logger
import pl.margoj.server.api.plugin.MargoJPlugin
import pl.margoj.server.api.plugin.PluginLoader
import pl.margoj.server.api.plugin.PluginManager
import java.io.File

class JarPluginLoader(override val pluginManager: PluginManager) : PluginLoader
{
    override fun canLoad(file: File): Boolean
    {
        return file.name.endsWith(".jar")
    }

    override fun load(file: File): MargoJPlugin<*>
    {
        pluginManager.server.logger.info("Ładowanie pluginu: " + file)

        val classLoader = JarPluginClassLoader(this, file, this.javaClass.classLoader)
        classLoader.load()

        val plugin = classLoader.plugin!!
        plugin.server = this.pluginManager.server
        plugin.logger = LogManager.getLogger(plugin.annotation.name)
        (plugin.logger as Logger).level = plugin.server.logger.level

        return plugin
    }
}