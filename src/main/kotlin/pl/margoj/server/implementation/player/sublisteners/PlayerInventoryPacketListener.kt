package pl.margoj.server.implementation.player.sublisteners

import pl.margoj.mrf.item.ItemCategory
import pl.margoj.server.api.inventory.map.MAP_LAYERS
import pl.margoj.server.implementation.item.ItemStackImpl
import pl.margoj.server.implementation.network.protocol.IncomingPacket
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.player.PlayerConnection

class PlayerInventoryPacketListener(connection: PlayerConnection) : PlayerPacketSubListener(connection, onlyOnPlayer = true)
{
    override fun handle(packet: IncomingPacket, out: OutgoingPacket, query: Map<String, String>): Boolean
    {
        val player = player!!
        val inventory = player.inventory
        val server = player.server

        if (packet.type == "moveitem")
        {
            val id = query["id"]?.toLong()
            val item = if (id == null) null else inventory.getItemstackById(id) as? ItemStackImpl

            this.checkForMaliciousData(item == null || item.owner != inventory, "invalid item")
            item!!

            if (inventory.isEquipedBag(item) != null && query["st"] != "0")
            {
                return true
            }
            this.checkForMaliciousData(inventory[item.ownerIndex!!] !== item, "invalid item at invalid index")

            when (query["st"]?.toInt())
            {
                -2 ->
                {
                    // destroy item
                    inventory[item.ownerIndex!!] = null
                }
                -1 ->
                {
                    // drop item
                    val location = player.location
                    if(!location.town!!.inventory.addItem(location.x, location.y, item))
                    {
                        out.addAlert("Na tej pozycji leżą już przynajmniej $MAP_LAYERS przedmioty!")
                    }
                }
                0 ->
                {
                    // move in bag
                    // TODO: validate position
                    val x = query["x"]?.toInt()
                    val margoY = query["y"]?.toInt()

                    this.checkForMaliciousData(x == null || margoY == null, "no coordinates provided")
                    x!!
                    margoY!!

                    val (y, bagId) = inventory.margoYToRealYAndBagId(margoY)

                    val equipedBag = inventory.isEquipedBag(item)
                    if (equipedBag != null)
                    {
                        if (!inventory.getBagInventory(equipedBag).isEmpty())
                        {
                            out.addAlert("Aby zdjąć torbę, należy ją najpierw opróżnić!")
                            return true
                        }
                        else if(bagId == equipedBag)
                        {
                            out.addAlert("Nie można ściągnąć torby do niej samej!")
                            return true
                        }
                    }

                    val bag = inventory.getBagInventory(bagId)
                    if (bag.getItemAt(x, y) == null)
                    {
                        bag.setItemAt(x, y, item)
                    }
                }
                1 ->
                {
                    // equip
                    val currentItemIndex = item.ownerIndex!!
                    // TODO: check requirements
                    val previousEqupment = inventory.equipment.tryEquip(item)

                    if (previousEqupment != null)
                    {
                        inventory[currentItemIndex] = previousEqupment
                    }
                }
                9 ->
                {
                    // purse
                    if (inventory.equipment.purse == null)
                    {
                        inventory.equipment.purse = item
                    }
                }
                20, 21, 22, 26 ->
                {
                    // bags
                    val realBagId = when (query["st"]?.toInt()!!)
                    {
                        20 -> 0
                        21 -> 1
                        22 -> 2
                        26 -> 3
                        else -> -1
                    }

                    this.checkForMaliciousData(realBagId == -1, "invalid bag id ${query["st"]}")

                    var putToBag = false

                    if (item.item.margoItem.itemCategory == ItemCategory.BAGS)
                    {
                        val equip = query["put"]?.toInt() ?: 1 == 1

                        if (equip)
                        {
                            if (inventory.getBag(realBagId) == null)
                            {
                                inventory.setBag(realBagId, item)
                            }
                        }
                        else
                        {
                            putToBag = true
                        }
                    }
                    else
                    {
                        putToBag = true
                    }

                    if (putToBag)
                    {
                        val targetBag = inventory.getBag(realBagId)
                        if (targetBag != null)
                        {
                            inventory.getBagInventory(realBagId).tryToPut(item)
                        }
                    }
                }
                else -> this.reportMaliciousData("invalid slot ${query["st"]}")
            }
        }
        else if(packet.type == "takeitem")
        {
            val location = player.location
            val item = location.town!!.inventory.getItemOnTop(location.x, location.y)
            if(item != null)
            {
                player.inventory.tryToPut(item)
            }
        }

        return true
    }
}