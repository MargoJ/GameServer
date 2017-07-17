package pl.margoj.server.implementation.inventory.player

import pl.margoj.server.api.inventory.ItemStack
import pl.margoj.server.api.inventory.player.PlayerEquipment

class PlayerEquipmentViewImpl(val owner: PlayerInventoryImpl) : PlayerEquipment
{
    override val allItems: Array<ItemStack?>
        get()
        {
            val all = arrayOfNulls<ItemStack?>(this.size)
            System.arraycopy(this.owner.allItems, 0, all, 0, this.size)
            return all
        }

    override val size: Int = 9

    override var helmet: ItemStack?
        get() = this.owner[0]
        set(value)
        {
            this.owner[0] = value
        }

    override var ring: ItemStack?
        get() = this.owner[1]
        set(value)
        {
            this.owner[1] = value
        }

    override var neckless: ItemStack?
        get() = this.owner[2]
        set(value)
        {
            this.owner[2] = value
        }

    override var gloves: ItemStack?
        get() = this.owner[3]
        set(value)
        {
            this.owner[3] = value
        }

    override var weapon: ItemStack?
        get() = this.owner[4]
        set(value)
        {
            this.owner[4] = value
        }

    override var armor: ItemStack?
        get() = this.owner[5]
        set(value)
        {
            this.owner[5] = value
        }

    override var helper: ItemStack?
        get() = this.owner[6]
        set(value)
        {
            this.owner[6] = value
        }

    override var boots: ItemStack?
        get() = this.owner[7]
        set(value)
        {
            this.owner[7] = value
        }

    override var purse: ItemStack?
        get() = this.owner[8]
        set(value)
        {
            this.owner[8] = value
        }

    override fun get(index: Int): ItemStack?
    {
        if (index >= 9)
        {
            throw ArrayIndexOutOfBoundsException(index)
        }

        return this.owner[index]
    }

    override fun set(index: Int, item: ItemStack?): ItemStack?
    {
        if (index >= 9)
        {
            throw ArrayIndexOutOfBoundsException(index)
        }

        return this.owner.set(index, item)
    }
}