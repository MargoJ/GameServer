package pl.margoj.server.implementation.inventory.player

import pl.margoj.server.api.inventory.ItemStack
import pl.margoj.server.api.inventory.player.PlayerEquipment
import pl.margoj.server.implementation.inventory.WrappedInventory
import pl.margoj.server.implementation.item.ItemStackImpl

class PlayerEquipmentViewImpl(override val owner: PlayerInventoryImpl) : PlayerEquipment, WrappedInventory
{
    override val size: Int = 9

    override val allItems: List<ItemStackImpl?> by lazy { this.owner.allItems.subList(0, this.size - 1) }

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