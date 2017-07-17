package pl.margoj.server.implementation.inventory

import pl.margoj.server.api.inventory.Inventory
import pl.margoj.server.api.inventory.ItemStack

open class AbstractInventoryImpl(size: Int) : Inventory
{
    override val allItems: Array<ItemStack?> = arrayOfNulls(size)
    override val size: Int = this.allItems.size

    override fun get(index: Int): ItemStack?
    {
        return allItems[index]
    }

    override fun set(index: Int, item: ItemStack?): ItemStack?
    {
        val old = this.allItems[index]
        this.allItems[index] = item
        return old
    }
}