package pl.margoj.server.implementation.inventory

import pl.margoj.server.api.inventory.Inventory
import pl.margoj.server.api.inventory.ItemStack
import pl.margoj.server.api.utils.ListFromMap
import pl.margoj.server.implementation.item.ItemStackImpl
import pl.margoj.server.implementation.network.protocol.jsons.ItemObject

abstract class AbstractInventoryImpl(final override val size: Int) : Inventory
{
    private val internalItems = hashMapOf<Int, ItemStackImpl?>()
    override val allItems: ListFromMap<ItemStackImpl?> = ListFromMap(this.internalItems, this.size)

    override fun get(index: Int): ItemStackImpl?
    {
        return allItems[index]
    }

    override fun set(index: Int, item: ItemStack?): ItemStackImpl?
    {
        item as ItemStackImpl?
        val old = this.allItems[index]

        if (item != null && item == old)
        {
            return null
        }

        if (item != null)
        {
            this.allItems[index] = item

            if (item.owner != null)
            {
                item.owner!!.notifyItemTransfer(this, item)
            }

            item.owner = this
            item.ownerIndex = index
        }
        else
        {
            this.internalItems.remove(index)
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

        if (this[item.ownerIndex!!] != item)
        {
            throw IllegalArgumentException("item is not at 'item.ownerIndex' in inventory 'item.owner'")
        }

        this[item.ownerIndex!!] = null
    }

    abstract fun createPacketFor(item: ItemStackImpl): ItemObject?
}