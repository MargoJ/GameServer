package pl.margoj.server.implementation.commands.defaults

import pl.margoj.server.implementation.commands.CommandsManagerImpl


object DefaultCommands
{
    fun registerDefaults(commandsManagerImpl: CommandsManagerImpl)
    {
        commandsManagerImpl.registerCoreListener(HelpCommand(), "help", "h")
        commandsManagerImpl.registerCoreListener(TeleportCommand(), "teleport", "tp")
        commandsManagerImpl.registerCoreListener(TownsCommand(), "towns")
        commandsManagerImpl.registerCoreListener(ItemCommand(), "item")
        commandsManagerImpl.registerCoreListener(AddXPCommand(), "addxp")
        commandsManagerImpl.registerCoreListener(StopCommand(), "stop")
        commandsManagerImpl.registerCoreListener(CacheCommand(), "cache")
        commandsManagerImpl.registerCoreListener(ConfirmCommand(), "confirm")

        if(commandsManagerImpl.server.debugModeEnabled)
        {
            commandsManagerImpl.registerCoreListener(TestNpcCommand(), "testnpc")
            commandsManagerImpl.registerCoreListener(TestBattleCommand(), "testbattle", "tb")
        }
    }
}