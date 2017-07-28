package pl.margoj.server.implementation.inventory.map

import pl.margoj.mrf.map.MargoMap
import pl.margoj.server.api.inventory.ItemStack
import pl.margoj.server.api.inventory.map.MAP_LAYERS
import pl.margoj.server.api.inventory.map.MapInventory
import pl.margoj.server.implementation.inventory.AbstractInventoryImpl
import pl.margoj.server.implementation.item.ItemLocation
import pl.margoj.server.implementation.item.ItemStackImpl
import pl.margoj.server.implementation.map.TownImpl
import pl.margoj.server.implementation.network.protocol.jsons.ItemObject

class MapInventoryImpl : AbstractInventoryImpl(MargoMap.MAX_SIZE * MargoMap.MAX_SIZE * MAP_LAYERS), MapInventory
{
    internal lateinit var mapId: String
    override lateinit var map: TownImpl

    override fun getItemAt(x: Int, y: Int, index: Int): ItemStack?
    {
        return this[this.getIndexFor(x, y, index)]
    }

    override fun setItemAt(x: Int, y: Int, index: Int, item: ItemStack?)
    {
        this[this.getIndexFor(x, y, index)] = item
    }

    override fun getItemsAt(x: Int, y: Int): Collection<ItemStackImpl?>
    {
        val items = ArrayList<ItemStackImpl?>(MAP_LAYERS)

        for (layer in 0..MAP_LAYERS - 1)
        {
            items.add(this.allItems[this.getIndexFor(x, y, layer)])
        }

        return items
    }

    override fun setItemsAt(x: Int, y: Int, items: Collection<ItemStack>)
    {
        val iterator = items.iterator()

        for (layer in 0..MAP_LAYERS - 1)
        {
            this.allItems[this.getIndexFor(x, y, layer)] = if (iterator.hasNext()) (iterator.next() as ItemStackImpl) else null
        }
    }

    override fun addItem(x: Int, y: Int, itemStack: ItemStack): Boolean
    {
        for (layer in 0..MAP_LAYERS - 1)
        {
            if (this.getItemAt(x, y, layer) == null)
            {
                this.setItemAt(x, y, layer, itemStack)
                return true
            }
        }
        return false
    }

    override fun getItemOnTop(x: Int, y: Int): ItemStack?
    {
        for (layer in MAP_LAYERS - 1 downTo 0)
        {
            val item = this.getItemAt(x, y, layer)
            if (item != null)
            {
                return item
            }
        }

        return null
    }

    override fun tryToPut(item: ItemStack): Boolean
    {
        throw IllegalStateException("illegal on MapInventory")
    }

    override fun createPacketFor(item: ItemStackImpl): ItemObject?
    {
        if (item.owner != this)
        {
            throw IllegalStateException("Can't create a packet for non-owned itemstack")
        }

        val index = item.ownerIndex!!
        val packet = this.map.server.itemManager.createItemObject(item)

        packet.location = ItemLocation.MAP.margoType
        packet.slot = 0

        val (_, x, y) = this.indexToData(index)
        packet.x = x
        packet.y = y

        return packet
    }

    private fun getIndexFor(x: Int, y: Int, index: Int): Int
    {
        val layerSize = MargoMap.MAX_SIZE * MargoMap.MAX_SIZE
        return layerSize * index + x * MargoMap.MAX_SIZE + y
    }

    private fun indexToData(index: Int): Triple<Int, Int, Int>
    {
        val layerSize = MargoMap.MAX_SIZE * MargoMap.MAX_SIZE
        val layer = (index / layerSize)
        val positionRest = index % layerSize
        val x = positionRest / MargoMap.MAX_SIZE
        val y = positionRest % MargoMap.MAX_SIZE
        return Triple(layer, x, y)
    }
}