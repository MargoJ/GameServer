package pl.margoj.server.implementation.resources

import com.twmacinta.util.MD5
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.Validate
import pl.margoj.mrf.bundle.MountResourceBundle
import pl.margoj.mrf.bundle.local.MargoMRFResourceBundle
import pl.margoj.mrf.bundle.local.MountedResourceBundle
import pl.margoj.server.implementation.ServerImpl
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class ResourceBundleManager(val server: ServerImpl, val resourceDirectory: File, val mountDirectory: File)
{
    private val resources_ = ArrayList<String>()
    private var currentMountPoint: File? = null
    var currentBundleFile = File("cache/.current.mrf")
    var currentBundle: MountResourceBundle? = null
        private set
    var bundlesCacheIndex = File("cache/bundles_index.json")
    var currentBundleName: String? = null

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

        MD5CacheUtils.ensureIndexFileExists(this.bundlesCacheIndex)
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

        this.currentBundleName = name
        this.currentMountPoint = File(this.mountDirectory, this.currentBundleName)

        val cached = MD5CacheUtils.getMD5FromCache(this.bundlesCacheIndex, name)
        val current = MD5.asHex(MD5.getHash(file))

        if (cached == current)
        {
            server.logger.info("Nie wykryto zmian w zestawie zasobow, uzywam tylko cache...")
            this.currentBundle = MountedResourceBundle(this.currentMountPoint!!)
            return
        }

        MD5CacheUtils.updateMD5Cache(this.bundlesCacheIndex, name, current)

        FileUtils.deleteDirectory(currentMountPoint)
        currentMountPoint!!.mkdirs()

        Files.copy(file.toPath(), this.currentBundleFile.toPath(), StandardCopyOption.REPLACE_EXISTING)

        this.currentBundle = MargoMRFResourceBundle(this.currentBundleFile, this.currentMountPoint!!)

        FileOutputStream(File(this.currentMountPoint, "index.json")).use {
            IOUtils.copy(this.currentBundle!!.createIndex(this.currentBundle!!.resources), it)
        }

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
            catch (e: Exception)
            {
                throw IllegalStateException("Couldn't close resource bundle (${currentBundle::class.java})", e)
            }

            server.logger.trace("Zamyknięto zestaw zasobów")
        }

        FileUtils.deleteDirectory(this.currentMountPoint)
        this.currentBundleFile.delete()

        this.currentBundle = null
        server.logger.info("Zestaw zasobów został odładowany")
        return true
    }
}