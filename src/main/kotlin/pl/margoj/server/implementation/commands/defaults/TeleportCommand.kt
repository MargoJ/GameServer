package pl.margoj.server.implementation.commands.defaults

import pl.margoj.server.api.commands.Arguments
import pl.margoj.server.api.commands.CommandListener
import pl.margoj.server.api.commands.CommandSender
import pl.margoj.server.api.map.Location
import pl.margoj.server.api.player.Player

class TeleportCommand : CommandListener
{
    override fun commandPerformed(command: String, sender: CommandSender, args: Arguments)
    {
        args.ensureTrue({ args.has(0) }, "Prawidłowe użycie: .tp &lt;mapa> [x] [y] - teleportuje na wybraną mape\"")
        args.ensureTrue({ sender is Player }, "Tylko gracz moze wykonać tą komende")
        sender as Player

        val town = sender.server.getTownById(args.asString(0))

        args.ensureNotNull(town, "Nie znaleziono miasta ${args.asString(0)}")
        town!!

        var targetX: Int? = if (args.has(2)) args.asInt(1) else null
        var targetY: Int? = if (args.has(2)) args.asInt(2) else null

        if (targetX == null || targetY == null)
        {
            var x = 0
            var y = 0

            loop@
            while (x < town.width)
            {
                while (y < town.height)
                {
                    if (!town.collisions[x][y])
                    {
                        targetX = x
                        targetY = y
                        break@loop
                    }
                    y++
                }
                x++
            }
        }

        args.ensureTrue({ targetX != null && targetY != null }, "Nie znaleziono miejsca do teleportacji na mapie ${args.asString(0)}")
        targetX!!
        targetY!!

        sender.logToConsole("Teleportuje...")
        sender.teleport(Location(town, targetX, targetY))
    }
}