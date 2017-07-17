package pl.margoj.server.implementation.inventory.player

import pl.margoj.server.api.inventory.ItemStack
import pl.margoj.server.api.inventory.player.PlayerBagInventory

class BagInventoryInventoryViewImpl(val owner: PlayerInventoryImpl, val startingY: Int) : PlayerBagInventory
{
    /** 7 * 6 */
    override val size: Int = 42

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

    override val allItems: Array<ItemStack?>
        get()
        {
            val all = arrayOfNulls<ItemStack?>(this.size)
            System.arraycopy(this.owner.bagInventories, this.indexToRealIndex(0), all, 0, this.size)
            return all
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