package pl.margoj.server.implementation.inventory.player

import pl.margoj.server.api.inventory.ItemStack
import pl.margoj.server.api.inventory.player.PlayerBagInventory
import pl.margoj.server.implementation.inventory.WrappedInventory
import pl.margoj.server.implementation.item.ItemStackImpl

class BagInventoryInventoryViewImpl(override val owner: PlayerInventoryImpl, val startingY: Int) : PlayerBagInventory, WrappedInventory
{
    /** 7 * 6 */
    override val size: Int = 42

    override val allItems: List<ItemStackImpl?> by lazy { this.owner.allItems.subList(this.indexToRealIndex(0), this.indexToRealIndex(0) + size - 1) }

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

    private fun indexToRealIndex(index: Int): Int
    {
        return 13 + (startingY * 7) + index
    }

    private fun coordinatesToIndex(x: Int, y: Int): Int
    {
        return y * 7 + x;
    }
}