package pl.margoj.server.implementation.commands.defaults

import pl.margoj.mrf.map.objects.mapspawn.MapSpawnObject
import pl.margoj.server.api.commands.Arguments
import pl.margoj.server.api.commands.CommandListener
import pl.margoj.server.api.commands.CommandSender
import pl.margoj.server.api.map.Location
import pl.margoj.server.api.player.Player
import pl.margoj.server.implementation.map.TownImpl

class TeleportCommand : CommandListener
{
    override fun commandPerformed(command: String, sender: CommandSender, args: Arguments)
    {
        args.ensureTrue({ args.has(0) }, "Prawidłowe użycie: .tp &lt;mapa> [x] [y] - teleportuje na wybraną mape\"")
        args.ensureTrue({ sender is Player }, "Tylko gracz moze wykonać tą komende")
        sender as Player

        val town = sender.server.getTownById(args.asString(0)) as TownImpl?

        args.ensureNotNull(town, "Nie znaleziono miasta ${args.asString(0)}")
        town!!

        var targetX: Int? = if (args.has(2)) args.asInt(1) else null
        var targetY: Int? = if (args.has(2)) args.asInt(2) else null

        if (targetX == null || targetY == null)
        {
            val spawnPoint = town.objects.find { it is MapSpawnObject }
            if (spawnPoint != null)
            {
                targetX = spawnPoint.position.x
                targetY = spawnPoint.position.y
            }
            else
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
        }

        args.ensureTrue({ targetX != null && targetY != null }, "Nie znaleziono miejsca do teleportacji na mapie ${args.asString(0)}")
        targetX!!
        targetY!!

        sender.logToConsole("Teleportuje...")
        sender.server.gameLogger.info("${sender.name}: .tp ${town.id}[${town.name}] $targetX $targetY")

        sender.teleport(Location(town, targetX, targetY))
    }
}