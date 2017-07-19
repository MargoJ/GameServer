package pl.margoj.server.implementation.inventory

import pl.margoj.server.api.inventory.Inventory
import pl.margoj.server.api.inventory.ItemStack
import pl.margoj.server.implementation.item.ItemStackImpl
import pl.margoj.server.implementation.network.protocol.jsons.ItemObject
import java.util.Collections

abstract class AbstractInventoryImpl(size: Int) : Inventory
{
    override val allItems: ArrayList<ItemStackImpl?> = ArrayList(Collections.nCopies(size, null))
    override val size: Int = this.allItems.size

    override fun get(index: Int): ItemStack?
    {
        return allItems[index]
    }

    override fun set(index: Int, item: ItemStack?): ItemStack?
    {
        item as ItemStackImpl?
        val old = this.allItems[index]

        if(item != null && item == old)
        {
            return null
        }

        this.allItems[index] = item

        if (item != null)
        {
            if (item.owner != null)
            {
                item.owner!!.notifyItemTransfer(this, item)
            }

            item.owner = this
            item.ownerIndex = index
        }

        if (old != null)
        {
            old.owner = null
        }

        return old
    }

    open fun notifyItemTransfer(to: AbstractInventoryImpl, item: ItemStackImpl)
    {
        if (item.owner != this)
        {
            throw IllegalArgumentException("item.owner != this")
        }

        if (this.get(item.ownerIndex!!) != item)
        {
            throw IllegalArgumentException("item is not at 'item.ownerIndex' in inventory 'item.owner'")
        }

        this.set(item.ownerIndex!!, null)
    }

    abstract fun createPacketFor(item: ItemStackImpl): ItemObject?
}