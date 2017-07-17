package pl.margoj.server.implementation.inventory.player

import pl.margoj.server.api.inventory.player.PlayerBagInventory
import pl.margoj.server.api.inventory.player.PlayerEquipment
import pl.margoj.server.api.inventory.player.PlayerInventory
import pl.margoj.server.implementation.inventory.AbstractInventoryImpl
import pl.margoj.server.implementation.item.ItemLocation
import pl.margoj.server.implementation.item.ItemStackImpl
import pl.margoj.server.implementation.network.protocol.jsons.ItemObject
import pl.margoj.server.implementation.player.PlayerImpl

/**
 * BAG is (6 * 7) = 42
 * Players has 4 bags so: 4 * 42 = 168
 * 9 slots for equipment and 4 for bags
 * 168 + 9 + 4 = 181
 */
const val PLAYER_INVENTORY_SIZE: Int = 181

class PlayerInventoryImpl(override val player: PlayerImpl) : AbstractInventoryImpl(PLAYER_INVENTORY_SIZE), PlayerInventory
{
    override val equipment: PlayerEquipment = PlayerEquipmentViewImpl(this)

    override val bagInventories: Array<PlayerBagInventory> = Array(4, { id -> BagInventoryInventoryViewImpl(this, id * 6) })

    fun createPacketFor(index: Int): ItemObject?
    {
        val item = this[index] ?: return null

        val packet = this.player.server.itemManager.createItemObject(item as ItemStackImpl)

        packet.own = player.id
        packet.location = ItemLocation.PLAYERS_INVENTORY.margoType

        when (index)
        {
            in 0..8 ->
            {
                packet.slot = index + 1
            }
            in 9..11 ->
            {
                packet.slot = index + 11
            }
            12 ->
            {
                packet.slot = 26
            }
            else ->
            {
                packet.slot = 0
                val realIndex = index - 13
                var y = realIndex / 7
                if(y >= 18)
                {
                    y += 18
                }

                packet.y = y
                packet.x = realIndex % 7
            }
        }

        return packet
    }
}