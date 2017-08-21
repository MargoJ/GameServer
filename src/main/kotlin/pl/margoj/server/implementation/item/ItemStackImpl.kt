package pl.margoj.server.implementation.item

import pl.margoj.mrf.item.ItemProperty
import pl.margoj.server.api.inventory.ItemStack
import pl.margoj.server.implementation.inventory.AbstractInventoryImpl
import pl.margoj.server.implementation.inventory.player.ItemTracker
import pl.margoj.server.implementation.network.protocol.jsons.ItemObject
import java.util.Collections
import java.util.WeakHashMap

class ItemStackImpl(itemManager: ItemManager, override val item: ItemImpl, override val id: Long) : ItemStack
{
    init
    {
        itemManager.validate(this.id)
    }

    val additionalProperties = hashMapOf<ItemProperty<*>, Any>()

    var owner: AbstractInventoryImpl? = null
        set(value)
        {
            field = value
            this.requestUpdate()
        }

    var ownerIndex: Int? = null
        set(value)
        {
            field = value
            this.requestUpdate()
        }

    var trackers: MutableSet<ItemTracker> = Collections.newSetFromMap(WeakHashMap<ItemTracker, Boolean>())

    fun requestUpdate()
    {
        for (tracker in this.trackers)
        {
            tracker.forceUpdate.add(this)
        }
    }

    fun createPacket(): ItemObject
    {
        return owner!!.createPacketFor(this)!!
    }

    fun createDeletePacket(): ItemObject
    {
        return ItemObject(id = this.id, hid = this.id, delete = 1)
    }

    override fun equals(other: Any?): Boolean
    {
        return (other is ItemStackImpl) && other.id == this.id
    }

    override fun hashCode(): Int
    {
        return java.lang.Long.hashCode(this.id)
    }
}