package pl.margoj.server.implementation.commands.defaults

import pl.margoj.server.implementation.commands.CommandsManagerImpl
import pl.margoj.server.implementation.commands.defaults.admin.*
import pl.margoj.server.implementation.commands.defaults.debug.TestBattleCommand
import pl.margoj.server.implementation.commands.defaults.debug.TestNpcCommand
import pl.margoj.server.implementation.commands.defaults.dev.CacheCommand
import pl.margoj.server.implementation.commands.defaults.standard.ConfirmCommand
import pl.margoj.server.implementation.commands.defaults.standard.HelpCommand


object DefaultCommands
{
    fun registerDefaults(commandsManagerImpl: CommandsManagerImpl)
    {
        // all commands
        commandsManagerImpl.registerCoreListener(HelpCommand(), "help", "h")
        commandsManagerImpl.registerCoreListener(ConfirmCommand(), "confirm")
        commandsManagerImpl.registerCoreListener(ListCommand(), "list")

        // administration commands
        commandsManagerImpl.registerCoreListener(AddXPCommand(), "addxp")
        commandsManagerImpl.registerCoreListener(InfoCommand(), "info")
        commandsManagerImpl.registerCoreListener(ItemCommand(), "item")
        commandsManagerImpl.registerCoreListener(KillCommand(commandsManagerImpl.server), "kill")
        commandsManagerImpl.registerCoreListener(StopCommand(), "stop")
        commandsManagerImpl.registerCoreListener(TeleportCommand(), "teleport", "tp")
        commandsManagerImpl.registerCoreListener(TownsCommand(), "towns")

        // dev commands
        commandsManagerImpl.registerCoreListener(CacheCommand(), "cache")

        // debug commands
        if (commandsManagerImpl.server.debugModeEnabled)
        {
            commandsManagerImpl.registerCoreListener(TestNpcCommand(), "testnpc")
            commandsManagerImpl.registerCoreListener(TestBattleCommand(), "testbattle", "tb")
        }
    }
}