package pl.margoj.server.implementation.commands.defaults

import pl.margoj.server.implementation.commands.CommandsManagerImpl


object DefaultCommands
{
    fun registerDefaults(commandsManagerImpl: CommandsManagerImpl)
    {
        commandsManagerImpl.registerCoreListener(TeleportCommand(), "teleport", "tp")
        commandsManagerImpl.registerCoreListener(TownsCommand(), "towns")
        commandsManagerImpl.registerCoreListener(TestInventoryCommand(), "testinventory", "testinv")
        commandsManagerImpl.registerCoreListener(AddXPCommand(), "addxp")
    }
}