package pl.margoj.server.implementation.resources

import org.apache.commons.io.IOUtils
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.graphics.GraphicDeserializer
import pl.margoj.mrf.item.ItemProperties
import pl.margoj.mrf.item.serialization.ItemDeserializer
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.serialization.MapDeserializer
import pl.margoj.mrf.map.tileset.AutoTileset
import pl.margoj.mrf.map.tileset.Tileset
import pl.margoj.mrf.map.tileset.TilesetFile
import pl.margoj.mrf.npc.NpcScript
import pl.margoj.mrf.npc.serialization.NpcScriptDeserializer
import pl.margoj.server.implementation.item.ItemImpl
import pl.margoj.server.implementation.map.TownImpl
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Collections
import javax.imageio.ImageIO

class ResourceLoader(val resourceBundleManager: ResourceBundleManager, val cacheDirectory: File)
{
    private var mapDeserializer: MapDeserializer? = null
    private var itemDeserializer = ItemDeserializer()
    private var scriptDeserializer = NpcScriptDeserializer()
    private var graphicDeserializer = GraphicDeserializer()

    private var numericId: Int = 1
    val mapCacheDirectory = File(this.cacheDirectory, "maps")
    val itemCacheDirectory = File(this.cacheDirectory, "items")
    val graphicsCacheDirectory = File(this.cacheDirectory, "graphics")
    val mapIndexFile = File(mapCacheDirectory, "index.json")

    init
    {
        this.reloadTilesets()

        MD5CacheUtils.ensureDirectoryExists(this.mapCacheDirectory)
        MD5CacheUtils.ensureDirectoryExists(this.itemCacheDirectory)
        MD5CacheUtils.ensureDirectoryExists(this.graphicsCacheDirectory)

        MD5CacheUtils.ensureIndexFileExists(this.mapIndexFile)
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

        val md5 = MD5CacheUtils.getMD5(bytes)
        val map = mapDeserializer!!.deserialize(ByteArrayInputStream(bytes))
        val currentMD5 = MD5CacheUtils.getMD5FromCache(this.mapIndexFile, map.id)
        val imageFile = File(this.mapCacheDirectory, map.id + ".png")

        if (!imageFile.exists() || md5 != currentMD5)
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
            MD5CacheUtils.updateMD5Cache(this.mapIndexFile, map.id, md5)
        }


        logger.info("Załadowano mape: $name")


        return TownImpl(this.resourceBundleManager.server, numericId++, map.id, map.name, map.width, map.height, map.collisions, map.metadata, map.objects, imageFile)
    }

    fun loadItem(id: String): ItemImpl?
    {
        val logger = this.resourceBundleManager.server.logger
        logger.trace("Ładuje przedmiot: $id")

        val bundle = this.resourceBundleManager.currentBundle
        val view = bundle!!.getResource(MargoResource.Category.ITEMS, id) ?: return null
        val resource = bundle.loadResource(view)
        val margoItem = this.itemDeserializer.deserialize(resource!!)

        val itemIcon = margoItem[ItemProperties.ICON]

        var itemIconName: String = ""
        var itemIconFile: File? = null

        if (itemIcon != null)
        {
            itemIconName = "${margoItem.id}.${itemIcon.format.extension}"
            itemIconFile = File(this.itemCacheDirectory, itemIconName)
            itemIconFile.delete()

            FileOutputStream(itemIconFile).use {
                IOUtils.write(itemIcon.image, it)
            }
        }

        return ItemImpl(margoItem, itemIconFile, itemIconName)
    }

    fun loadGraphic(id: String)
    {
        val logger = this.resourceBundleManager.server.logger
        logger.trace("Ładuje grafike: $id")

        val bundle = this.resourceBundleManager.currentBundle
        val view = bundle!!.getResource(MargoResource.Category.GRAPHIC, id)!!

        val inputStream = bundle.loadResource(view)!!
        val graphic = this.graphicDeserializer.deserialize(inputStream)

        val file = File(this.graphicsCacheDirectory, view.name)
        file.delete()

        FileOutputStream(file).use {
            IOUtils.copy(ByteArrayInputStream(graphic.icon.image), it)
        }

        logger.info("Załadowano grafike: $id")
    }

    fun loadScript(id: String): NpcScript?
    {
        val logger = this.resourceBundleManager.server.logger
        logger.trace("Ładuje skrypt: $id")

        val bundle = this.resourceBundleManager.currentBundle
        val view = bundle!!.getResource(MargoResource.Category.NPC_SCRIPTS, id) ?: return null
        val resource = bundle.loadResource(view)
        scriptDeserializer.fileName = id
        val script = scriptDeserializer.deserialize(resource!!)

        return script
    }

    fun reloadTilesets()
    {
        val bundle = this.resourceBundleManager.currentBundle!!
        val resources = bundle.getResourcesByCategory(MargoResource.Category.TILESETS)

        val tilesetFiles = ArrayList<TilesetFile>(resources.size)
        val tilesets = ArrayList<Tileset>(resources.size)

        resources.forEach { resource ->
            bundle.loadResource(resource) // make sure it will unpack and be avaialbe using getLocalFile

            tilesetFiles.add(TilesetFile(bundle.getLocalFile(resource), resource.id, resource.id.startsWith("auto-")))
        }

        val autoTileset = AutoTileset(AutoTileset.AUTO, tilesetFiles.filter(TilesetFile::auto))
        tilesets.add(autoTileset)
        tilesets.addAll(tilesetFiles.filter { !it.auto }.map { Tileset(it.name, it.image, Collections.singletonList(it)) })

        this.mapDeserializer = MapDeserializer(tilesets)
    }
}