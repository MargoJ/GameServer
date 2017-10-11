package pl.margoj.server.implementation.inventory.player

import pl.margoj.mrf.item.ItemProperties
import pl.margoj.server.api.inventory.ItemStack
import pl.margoj.server.api.inventory.player.PlayerBagInventory
import pl.margoj.server.implementation.inventory.WrappedInventory
import pl.margoj.server.implementation.item.ItemImpl
import pl.margoj.server.implementation.item.ItemStackImpl

class BagInventoryInventoryViewImpl(override val owner: PlayerInventoryImpl, val startingY: Int) : PlayerBagInventory, WrappedInventory
{
    /** 7 * 6 */
    override val size: Int = 42

    private val firstIndex = this.indexToRealIndex(0)
    private val lastIndex = firstIndex + size - 1

    override val allItems: List<ItemStackImpl?> by lazy { this.owner.allItems.subList(firstIndex, lastIndex) }

    override fun getItemAt(x: Int, y: Int): ItemStack?
    {
        return owner[this.indexToRealIndex(this.coordinatesToIndex(x, y))]
    }

    override fun setItemAt(x: Int, y: Int, item: ItemStack?): ItemStack?
    {
        return owner.set(this.indexToRealIndex(this.coordinatesToIndex(x, y)), item)
    }

    override fun get(index: Int): ItemStack?
    {
        return owner[this.indexToRealIndex(index)]
    }

    override fun set(index: Int, item: ItemStack?): ItemStack?
    {
        return owner.set(this.indexToRealIndex(index), item)
    }

    override fun isEmpty(): Boolean
    {
        for (i in firstIndex..lastIndex)
        {
            if (this.owner[i] != null)
            {
                return false
            }
        }

        return true
    }

    override fun isFull(): Boolean
    {
        val size = (this.owner.getBag(this.startingY / 6)?.item as? ItemImpl)?.margoItem?.get(ItemProperties.SIZE) ?: return false
        var count = 0

        for(i in firstIndex..lastIndex)
        {
            if(this.owner[i] != null)
            {
                if(++count >= size)
                {
                    return true
                }
            }
        }

        return false
    }

    override fun tryToPut(item: ItemStack): Boolean
    {
        if(this.isFull())
        {
            return false
        }

        for (i in firstIndex..lastIndex)
        {
            if (this.owner[i] == null)
            {
                this.owner[i] = item
                return true
            }
        }

        return false
    }

    private fun indexToRealIndex(index: Int): Int
    {
        return 13 + (startingY * 7) + index
    }

    private fun coordinatesToIndex(x: Int, y: Int): Int
    {
        return y * 7 + x
    }
}