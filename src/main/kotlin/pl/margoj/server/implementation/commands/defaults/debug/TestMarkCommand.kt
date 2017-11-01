package pl.margoj.server.implementation.commands.defaults.debug

import pl.margoj.mrf.item.ItemProperties
import pl.margoj.server.api.commands.Arguments
import pl.margoj.server.api.commands.CommandListener
import pl.margoj.server.api.commands.CommandSender
import pl.margoj.server.api.player.Player
import pl.margoj.server.implementation.item.ItemStackImpl

class TestMarkCommand : CommandListener
{
    override fun commandPerformed(command: String, sender: CommandSender, args: Arguments)
    {
        sender as Player
        val item = sender.inventory.equipment.purse!! as ItemStackImpl
        item.setProperty(ItemProperties.NAME, "marked: ${System.currentTimeMillis()}")
        item.setProperty(ItemProperties.DESCRIPTION, "marked desc: ${System.currentTimeMillis()}")
        sender.sendMessage("ok")
    }
}