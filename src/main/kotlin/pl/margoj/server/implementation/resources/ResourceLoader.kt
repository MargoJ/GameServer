package pl.margoj.server.implementation.resources

import com.google.gson.JsonObject
import org.apache.commons.io.IOUtils
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.serialization.MapDeserializer
import pl.margoj.mrf.map.tileset.AutoTileset
import pl.margoj.mrf.map.tileset.Tileset
import pl.margoj.mrf.map.tileset.TilesetFile
import pl.margoj.server.implementation.map.TownImpl
import pl.margoj.server.implementation.utils.GsonUtils
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.*
import java.security.MessageDigest
import javax.imageio.ImageIO

class ResourceLoader(val resourceBundleManager: ResourceBundleManager)
{
    private var mapDeserializer: MapDeserializer? = null
    private var numericId: Int = 1
    private var cacheDirectory = File("mapcache")
    private var indexFile = File(cacheDirectory, "index.json")
    private var messageDigest: MessageDigest = MessageDigest.getInstance("MD5")

    init
    {
        this.reloadTilesets()

        if (!cacheDirectory.exists())
        {
            cacheDirectory.mkdirs()
        }
        if (!this.indexFile.exists())
        {
            this.indexFile.createNewFile()
            FileWriter(this.indexFile).use { it.write("{}") }
        }
    }

    fun loadMap(name: String): TownImpl?
    {
        val logger = this.resourceBundleManager.server.logger
        logger.trace("Ładuje mape: $name")

        val bundle = this.resourceBundleManager.currentBundle
        val view = bundle!!.getResource(MargoResource.Category.MAPS, name) ?: return null

        val inputStream = bundle.loadResource(view)!!
        val byteOutput = ByteArrayOutputStream()
        IOUtils.copy(inputStream, byteOutput)
        val bytes = byteOutput.toByteArray()

        messageDigest.reset()
        messageDigest.update(bytes)
        val digest = messageDigest.digest()
        val result = StringBuilder()

        for (byte in digest)
        {
            result.append(Integer.toString((byte.toInt() and 0xff) + 0x100, 16).substring(1))
        }

        val md5 = result.toString()
        val map = mapDeserializer!!.deserialize(ByteArrayInputStream(bytes))
        val currentMD5 = this.getMD5FromCache(map.id)
        val imageFile = File(this.cacheDirectory, map.id + ".png")

        if(!imageFile.exists() || md5 != currentMD5)
        {
            logger.trace("MD5: $md5, current: $currentMD5")

            val image = BufferedImage(map.width * 32, map.height * 32, BufferedImage.TYPE_INT_ARGB)
            val graphics = image.graphics
            graphics.color = Color.BLACK
            graphics.fillRect(0, 0, map.width * 32, map.height * 32)

            for (x in 0..(map.width - 1))
            {
                for (y in 0..(map.height - 1))
                {
                    val part = BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB)
                    val partGraphics = part.graphics

                    for (layer in 0..(MargoMap.LAYERS - 1))
                    {
                        map.fragments[x][y][layer].draw(partGraphics)
                    }

                    graphics.drawImage(part, x * 32, y * 32, null)
                }
            }

            ImageIO.write(image, "png", imageFile)
            this.updateMD5Cache(map.id, md5)
        }


        logger.info("Załadowano mape: $name")

        return TownImpl(numericId++, map.id, map.name, map.width, map.height, map.collisions, map, imageFile)
    }

    private fun getMD5FromCache(id: String): String?
    {
        FileReader(this.indexFile).use {
            val json = GsonUtils.parser.parse(it).asJsonObject
            val element = json[id]
            return if (element == null || !element.isJsonPrimitive || !element.asJsonPrimitive.isString) null else element.asJsonPrimitive.asString
        }
    }

    private fun updateMD5Cache(id: String, md5: String)
    {
        var json: JsonObject? = null

        FileReader(this.indexFile).use {
            json = GsonUtils.parser.parse(it).asJsonObject
        }

        json!!.addProperty(id, md5)

        FileWriter(this.indexFile).use {
            it.write(GsonUtils.gson.toJson(json))
        }
    }

    fun reloadTilesets()
    {
        // TODO: Reload from bundle

        val directory = File("temptilesets")
        val files = directory.list()

        val tilesetFiles = ArrayList<TilesetFile>(files.size)
        val tilesets = ArrayList<Tileset>(files.size)

        files.filter { it.endsWith(".png") }.forEach {
            file ->
            val name = file.substring(0, file.lastIndexOf('.'))
            tilesetFiles.add(TilesetFile(File(directory, file), name, file.startsWith("auto-")))
        }

        val autoTileset = AutoTileset(AutoTileset.AUTO, tilesetFiles.filter(TilesetFile::auto))
        tilesets.add(autoTileset)
        tilesets.addAll(tilesetFiles.filter { !it.auto }.map { Tileset(it.name, it.image) })

        this.mapDeserializer = MapDeserializer(tilesets)
    }
}