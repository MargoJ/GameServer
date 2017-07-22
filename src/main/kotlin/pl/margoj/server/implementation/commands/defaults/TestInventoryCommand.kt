package pl.margoj.server.implementation.commands.defaults

import pl.margoj.mrf.item.ItemCategory
import pl.margoj.server.api.commands.Arguments
import pl.margoj.server.api.commands.CommandListener
import pl.margoj.server.api.commands.CommandSender
import pl.margoj.server.api.player.Player
import pl.margoj.server.implementation.item.ItemImpl

class TestInventoryCommand : CommandListener
{
    override fun commandPerformed(command: String, sender: CommandSender, args: Arguments)
    {
        args.ensureTrue({ sender is Player }, "Tylko gracz moze wykonać tą komende")
        sender as Player

        if(sender.inventory.getBag(0) == null)
        {
            val item = sender.server.items.map { it as ItemImpl }.filter { it.margoItem.itemCategory == ItemCategory.BAGS }.first()

            sender.inventory.setBag(0, sender.server.newItemStack(item))
        }

        for (item in sender.server.items)
        {
            sender.inventory.tryToPut(sender.server.newItemStack(item))
        }

        sender.sendMessage("Ekwipunek przygotowany!")
    }
}