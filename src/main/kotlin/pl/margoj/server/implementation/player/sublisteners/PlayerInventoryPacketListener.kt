package pl.margoj.server.implementation.player.sublisteners

import pl.margoj.mrf.item.ItemCategory
import pl.margoj.mrf.item.ItemProperties
import pl.margoj.server.api.inventory.ItemStack
import pl.margoj.server.api.inventory.map.MAP_LAYERS
import pl.margoj.server.api.inventory.player.ItemIsOnCooldownException
import pl.margoj.server.api.inventory.player.ItemRequirementsNotMetException
import pl.margoj.server.api.utils.TimeFormatUtils
import pl.margoj.server.implementation.item.ItemStackImpl
import pl.margoj.server.implementation.item.pipeline.ItemPipelines
import pl.margoj.server.implementation.item.pipeline.drag.ItemDragPipelineData
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
            player.server.gameLogger.info("${player.name}: moveitem: id=${query["id"]}, slot = ${query["st"]}")

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
                    if (item[ItemProperties.CATEGORY] == ItemCategory.QUEST)
                    {
                        player.displayAlert("Tego przedmiotu nie można zniszczyć w tej chwili, jest on potrzebny do ukończenia trwającego questa.")
                        return true
                    }

                    player.server.gameLogger.info("${player.name}: item zniszczony: ${item.item.toSimpleString()}")

                    item.destroyItem()
                }
                -1 ->
                {
                    if (item[ItemProperties.SOUL_BOUND] || item[ItemProperties.PERM_BOUND])
                    {
                        player.displayAlert("Nie można upuszczać przedmiotów związanych z graczem!")
                        return true
                    }

                    if (item[ItemProperties.CATEGORY] == ItemCategory.QUEST)
                    {
                        player.displayAlert("Tego przedmiotu nie można upuścić w tej chwili, jest on potrzebny do ukończenia trwającego questa.")
                        return true
                    }

                    // drop item
                    val location = player.location
                    if (!location.town!!.inventory.addItem(location.x, location.y, item))
                    {
                        out.addAlert("Na tej pozycji leżą już przynajmniej $MAP_LAYERS przedmioty!")
                    }
                    else
                    {
                        player.server.gameLogger.info("${player.name}: item upusczony: ${item.item.toSimpleString()} na pozycji ${location.toSimpleString()}")
                    }
                }
                0 ->
                {
                    // move in bag
                    val x = query["x"]?.toInt()
                    val margoY = query["y"]?.toInt()

                    this.checkForMaliciousData(x == null || margoY == null, "no coordinates provided")
                    x!!
                    margoY!!

                    val (y, bagId) = inventory.margoYToRealYAndBagId(margoY)

                    val targetBag = inventory.getBag(bagId)
                    this.checkForMaliciousData(targetBag == null || y > 5, "invalid bag")

                    val equipedBag = inventory.isEquipedBag(item)
                    if (equipedBag != null)
                    {
                        if (!inventory.getBagInventory(equipedBag).isEmpty())
                        {
                            out.addAlert("Aby zdjąć torbę, należy ją najpierw opróżnić!")
                            return true
                        }
                        else if (bagId == equipedBag)
                        {
                            out.addAlert("Nie można ściągnąć torby do niej samej!")
                            return true
                        }
                    }

                    val bag = inventory.getBagInventory(bagId)

                    if (item !in bag && bag.isFull())
                    {
                        player.displayAlert("Brak wolnego miejsca w tej torbie!")
                        return true
                    }

                    val targetItem = bag.getItemAt(x, y) as? ItemStackImpl
                    if (targetItem != null)
                    {
                        ItemPipelines.ITEM_DRAG_PIPELINE.process(ItemDragPipelineData(item, targetItem, player))
                    }
                    else
                    {
                        val split = query["split"]?.toIntOrNull()

                        if (split != null)
                        {
                            val isSplittable = item[ItemProperties.MAX_AMOUNT] != 0
                            if (!isSplittable)
                            {
                                return true
                            }

                            val amount = item[ItemProperties.AMOUNT]
                            this.checkForMaliciousData(split >= amount, "split too big")

                            item.setProperty(ItemProperties.AMOUNT, amount - split)

                            val newItem = player.server.newItemStack(item.item)
                            newItem.cloneFrom(item)
                            newItem.setProperty(ItemProperties.AMOUNT, split)

                            bag.setItemAt(x, y, newItem)
                            return true
                        }

                        bag.setItemAt(x, y, item)
                    }
                }
                1 ->
                {
                    // equip
                    val currentItemIndex = item.ownerIndex!!

                    val previousEquipment: ItemStack?
                    try
                    {
                        previousEquipment = inventory.equipment.use(item)
                    }
                    catch (e: ItemRequirementsNotMetException)
                    {
                        player.displayAlert("Nie spełniasz wymagań koniecznych do używania tego przedmiotu!")
                        return true
                    }
                    catch (e: ItemIsOnCooldownException)
                    {
                        player.displayAlert("Zanim następny raz użyjesz przedmiotu, musisz poczekać jeszcze ${TimeFormatUtils.getReadableTime(e.left, true)}!")
                        return true
                    }

                    if (previousEquipment != null)
                    {
                        inventory[currentItemIndex] = previousEquipment
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
                            if (!inventory.getBagInventory(realBagId).tryToPut(item))
                            {
                                player.displayAlert("Brak wolnego miejsca w tej torbie!")
                                return true
                            }
                        }
                    }
                }
                else -> this.reportMaliciousData("invalid slot ${query["st"]}")
            }
        }
        else if (packet.type == "takeitem")
        {
            val location = player.location
            val item = location.town!!.inventory.getItemOnTop(location.x, location.y) as? ItemStackImpl
            if (item != null)
            {
                player.server.gameLogger.info("${player.name}: item podniesiony: ${item.item.toSimpleString()}, id=${item.id}, lokacja = ${location.toSimpleString()}")
                player.inventory.tryToPut(item)
            }
        }

        return true
    }
}