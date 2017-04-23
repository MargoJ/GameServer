package pl.margoj.server.implementation.resources

import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.Validate
import pl.margoj.mrf.bundle.MountResourceBundle
import pl.margoj.mrf.bundle.local.MargoMRFResourceBundle
import pl.margoj.server.implementation.ServerImpl
import java.io.File

class ResourceBundleManager(val server: ServerImpl, val resourceDirectory: File, val mountDirectory: File)
{
    private val resources_ = ArrayList<String>()
    private var currentMountPoint: File? = null
    var currentBundle: MountResourceBundle? = null
        private set


    init
    {
        if (!resourceDirectory.exists())
        {
            resourceDirectory.mkdirs()
        }

        Validate.isTrue(resourceDirectory.isDirectory, "Resources directory is not a directory")

        if (!mountDirectory.exists())
        {
            mountDirectory.mkdirs()
        }

        Validate.isTrue(mountDirectory.isDirectory, "Mount directory is not a directory")
    }

    val resources: Collection<String> get() = this.resources_

    fun reloadResourceList()
    {
        server.logger.trace("Przeładowuje liste zasobów")
        this.resources_.clear()

        for (name in this.resourceDirectory.list())
        {
            if (name.endsWith(".mrf"))
            {
                this.resources_.add(name.substring(0, name.length - ".mrf".length))
            }
        }

        server.logger.debug("Znaleziono ${resources.size} zasobów: ${this.resources}")
    }

    fun loadBundle(name: String)
    {
        server.logger.trace("Ładowanie zestawu zasobów \"$name\"")

        val file = File(this.resourceDirectory, "$name.mrf")
        if (!file.exists())
        {
            throw IllegalArgumentException("Bundle $name couldn't be find")
        }

        if (this.currentBundle != null)
        {
            this.unloadBundle()
        }

        this.currentMountPoint = File(this.mountDirectory, System.currentTimeMillis().toString())
        currentMountPoint?.mkdirs()
        this.currentBundle = MargoMRFResourceBundle(file, this.currentMountPoint!!)

        server.logger.debug("Załadowano zestaw zasobów $name, mrf=${file.absolutePath}, mountPoint=${currentMountPoint!!.absolutePath}")
    }

    fun unloadBundle(): Boolean
    {
        if (this.currentBundle == null)
        {
            return false
        }

        val currentBundle = this.currentBundle!!

        server.logger.trace("Odładowywuje zestaw zasobów")

        if (currentBundle is AutoCloseable)
        {
            server.logger.trace("Zamykam zestaw zasobów")

            try
            {
                currentBundle.close()
            }
            catch(e: Exception)
            {
                throw IllegalStateException("Couldn't close resource bundle (${currentBundle::class.java})", e)
            }

            server.logger.trace("Zamyknięto zestaw zasobów")
        }

        FileUtils.deleteDirectory(this.currentMountPoint)


        this.currentBundle = null
        server.logger.info("Zestaw zasobów został odładowany")
        return true
    }
}