package pl.margoj.server.implementation.resources

import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.tileset.AutoTileset
import pl.margoj.mrf.map.tileset.Tileset
import pl.margoj.mrf.map.tileset.TilesetFile
import pl.margoj.mrf.map.serialization.MapDeserializer
import pl.margoj.server.implementation.map.TownImpl
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO

class ResourceLoader(val resourceBundleManager: ResourceBundleManager)
{
    private var mapDeserializer: MapDeserializer? = null
    private var numericId: Int = 1

    init
    {
        this.reloadTilesets()
    }

    fun loadMap(name: String): TownImpl?
    {
        this.resourceBundleManager.server.logger.trace("Ładuje mape: $name")

        val bundle = this.resourceBundleManager.currentBundle
        val view = bundle!!.getResource(MargoResource.Category.MAPS, name) ?: return null

        val map = mapDeserializer!!.deserialize(bundle.loadResource(view)!!)

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

                for(layer in 0..(MargoMap.LAYERS - 1))
                {
                    map.fragments[x][y][layer].draw(partGraphics)
                }

                graphics.drawImage(part, x * 32, y * 32, null)
            }
        }

        val bytes = ByteArrayOutputStream()
        ImageIO.write(image, "png", bytes)

        this.resourceBundleManager.server.logger.info("Załadowano mape: $name")

        return TownImpl(numericId++, map.id, map.name, map.width, map.height, map.collisions, map, bytes.toByteArray())
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