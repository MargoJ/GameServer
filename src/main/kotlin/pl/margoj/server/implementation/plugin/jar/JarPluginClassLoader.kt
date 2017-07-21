package pl.margoj.server.implementation.plugin.jar

import org.reflections.Reflections
import pl.margoj.server.api.plugin.MargoJPlugin
import pl.margoj.server.api.plugin.Plugin
import java.io.File
import java.net.URLClassLoader

internal class JarPluginClassLoader(val loader: JarPluginLoader, val jarFile: File, val parentLoader: ClassLoader) : URLClassLoader(arrayOf(jarFile.toURI().toURL()), null)
{
    private var scanGlobally = true

    var plugin: MargoJPlugin<*>? = null
        private set

    fun load()
    {
        this.scanGlobally = false
        val classes = Reflections(this).getTypesAnnotatedWith(Plugin::class.java)
        this.scanGlobally = true

        if (classes.size == 0)
        {
            throw IllegalStateException("Couldn't find @Plugin annotation in ${this.jarFile}")
        }
        if (classes.size > 1)
        {
            throw IllegalStateException("Found multiple @Plugin annotations in ${this.jarFile}")
        }

        this.plugin = classes.iterator().next().newInstance() as MargoJPlugin<*>
    }

    override fun findClass(name: String): Class<*>
    {
        if (scanGlobally)
        {
            val possibleOutput = allClasses[name]
            if (possibleOutput != null)
            {
                return possibleOutput
            }
        }

        try
        {
            val out = super.findClass(name)
            allClasses.put(name, out)
            return out
        }
        catch (e: ClassNotFoundException)
        {
            return parentLoader.loadClass(name)
        }
    }

    companion object
    {
        private val allClasses = HashMap<String, Class<*>>(100)
    }
}