package pl.margoj.server.implementation.commands.defaults

import pl.margoj.mrf.item.ItemProperties
import pl.margoj.server.api.commands.Arguments
import pl.margoj.server.api.commands.CommandListener
import pl.margoj.server.api.commands.CommandSender
import pl.margoj.server.api.player.Player
import pl.margoj.server.implementation.item.ItemStackImpl

class TestInventoryCommand : CommandListener
{
    override fun commandPerformed(command: String, sender: CommandSender, args: Arguments)
    {
        args.ensureTrue({ sender is Player }, "Tylko gracz moze wykonać tą komende")
        sender as Player

        val someItem = sender.server.items.iterator().next()

        fun prepareTest(desc: String): ItemStackImpl
        {
            val itemStack = sender.server.newItemStack(someItem) as ItemStackImpl
            itemStack.additionalProperties.put(ItemProperties.DESCRIPTION, desc)
            return itemStack
        }

        val inventory = sender.inventory

        inventory.equipment.helmet = prepareTest("helm")
        inventory.equipment.ring = prepareTest("pierscien")
        inventory.equipment.neckless = prepareTest("naszyjnik")
        inventory.equipment.gloves = prepareTest("rekawice")
        inventory.equipment.weapon = prepareTest("bron")
        inventory.equipment.armor = prepareTest("armor")
        inventory.equipment.helper = prepareTest("pomocnicze")
        inventory.equipment.boots = prepareTest("buty")
        inventory.equipment.purse = prepareTest("sakwa")

        inventory.setBag(0, prepareTest("torba 1"))
        inventory.setBag(1, prepareTest("torba 2"))
        inventory.setBag(2, prepareTest("torba 3"))
        inventory.setBag(3, prepareTest("torba na klucze"))

        inventory.getBagInventory(0).setItemAt(0, 0, prepareTest("torba 1 lewe gora"))
        inventory.getBagInventory(0).setItemAt(6, 0, prepareTest("torba 1 prawo gora"))
        inventory.getBagInventory(0).setItemAt(0, 5, prepareTest("torba 1 lewe dol"))
        inventory.getBagInventory(0).setItemAt(6, 5, prepareTest("torba 1 prawo dol"))

        inventory.getBagInventory(1).setItemAt(0, 0, prepareTest("torba 2 lewe gora"))
        inventory.getBagInventory(1).setItemAt(6, 0, prepareTest("torba 2 prawo gora"))
        inventory.getBagInventory(1).setItemAt(0, 5, prepareTest("torba 2 lewe dol"))
        inventory.getBagInventory(1).setItemAt(6, 5, prepareTest("torba 2 prawo dol"))

        inventory.getBagInventory(2).setItemAt(0, 0, prepareTest("torba 3 lewe gora"))
        inventory.getBagInventory(2).setItemAt(6, 0, prepareTest("torba 3 prawo gora"))
        inventory.getBagInventory(2).setItemAt(0, 5, prepareTest("torba 3 lewe dol"))
        inventory.getBagInventory(2).setItemAt(6, 5, prepareTest("torba 3 prawo dol"))

        inventory.getBagInventory(3).setItemAt(0, 0, prepareTest("torba klucze lewe gora"))
        inventory.getBagInventory(3).setItemAt(6, 0, prepareTest("torba klucze prawo gora"))
        inventory.getBagInventory(3).setItemAt(0, 5, prepareTest("torba klucze lewe dol"))
        inventory.getBagInventory(3).setItemAt(6, 5, prepareTest("torba klucze prawo dol"))

        sender.sendMessage("Ekwipunek przygotowany!")
    }
}