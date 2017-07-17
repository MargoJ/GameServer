package pl.margoj.server.implementation.resources

import com.google.gson.JsonObject
import pl.margoj.server.implementation.utils.GsonUtils
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.security.MessageDigest

internal object MD5CacheUtils
{
    private var messageDigestContainer = ThreadLocal.withInitial { MessageDigest.getInstance("MD5") }

    fun getMD5(bytes: ByteArray): String
    {
        val messageDigest = messageDigestContainer.get()
        messageDigest.reset()
        messageDigest.update(bytes)
        val digest = messageDigest.digest()
        val result = StringBuilder()

        for (byte in digest)
        {
            result.append(Integer.toString((byte.toInt() and 0xff) + 0x100, 16).substring(1))
        }

        return result.toString()
    }

    fun ensureDirectoryExists(directory: File)
    {
        if (!directory.exists())
        {
            directory.mkdirs()
        }
    }

    fun ensureIndexFileExists(file: File)
    {
        if (!file.exists())
        {
            MD5CacheUtils.ensureDirectoryExists(file.parentFile)
            file.createNewFile()
            FileWriter(file).use { it.write("{}") }
        }
    }

    fun getMD5FromCache(indexFile: File, id: String): String?
    {
        FileReader(indexFile).use {
            val json = GsonUtils.parser.parse(it).asJsonObject
            val element = json[id]
            return if (element == null || !element.isJsonPrimitive || !element.asJsonPrimitive.isString) null else element.asJsonPrimitive.asString
        }
    }

    fun updateMD5Cache(indexFile: File, id: String, md5: String)
    {
        var json: JsonObject? = null

        FileReader(indexFile).use {
            json = GsonUtils.parser.parse(it).asJsonObject
        }

        json!!.addProperty(id, md5)

        FileWriter(indexFile).use {
            it.write(GsonUtils.gson.toJson(json))
        }
    }
}