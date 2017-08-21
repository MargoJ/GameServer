package pl.margoj.server.implementation.inventory.player

import pl.margoj.server.api.sync.Tickable
import pl.margoj.server.implementation.inventory.map.MapInventoryImpl
import pl.margoj.server.implementation.item.ItemStackImpl
import pl.margoj.server.implementation.player.PlayerImpl
import java.util.LinkedList

class ItemTracker(val player: PlayerImpl) : Tickable
{
    private val trackedItems = LinkedList<ItemStackImpl>()
    val forceUpdate = HashSet<ItemStackImpl>()
    var enabled: Boolean = false

    override fun tick(currentTick: Long)
    {
        if (this.enabled)
        {
            this.doTrack()
        }
    }

    override fun toString(): String
    {
        return "PlayerTickable(player=${this.player})"
    }

    fun reset()
    {
        this.trackedItems.clear()
    }

    fun doTrack()
    {
        for (possibleInventorySource in player.possibleInventorySources)
        {
            for (item in possibleInventorySource.allItems)
            {
                if (item == null || !this.shouldSee(item))
                {
                    continue
                }

                if (this.forceUpdate.contains(item) || !this.trackedItems.contains(item))
                {
                    this.trackItem(item)
                    this.trackedItems.add(item)
                    this.forceUpdate.remove(item)
                }
            }
        }

        val iter = this.trackedItems.iterator()

        while (iter.hasNext())
        {
            val item = iter.next()

            if (!this.shouldSee(item))
            {
                iter.remove()
                this.untrackItem(item)
            }
        }
    }

    private fun trackItem(item: ItemStackImpl)
    {
        item.trackers.add(this)
        this.player.connection.addModifier { it.addItem(item.createPacket()) }
    }

    private fun untrackItem(item: ItemStackImpl)
    {
        item.trackers.remove(this)
        this.player.connection.addModifier { it.addItem(item.createDeletePacket()) }
    }

    private fun shouldSee(item: ItemStackImpl?): Boolean
    {
        if (item?.owner == null)
        {
            return false
        }

        if (item.owner is PlayerInventoryImpl)
        {
            return item.owner == this.player.inventory
        }

        if (item.owner is MapInventoryImpl)
        {
            return (item.owner as MapInventoryImpl).map == this.player.location.town
        }

        return this.player.possibleInventorySources.contains(item.owner!!)
    }

}