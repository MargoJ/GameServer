package pl.margoj.server.implementation.commands.defaults.admin

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
        args.ensureTrue({ args.has(0) }, "Prawidłowe użycie: .tp &lt;mapa> [x] [y] [gracz] lub .tp &lt;mapa> [gracz] - teleportuje na wybraną mape\"")

        val town = sender.server.getTownById(args.asString(0)) as TownImpl?
        args.ensureNotNull(town, "Nie znaleziono miasta ${args.asString(0)}")
        town!!

        var target: Player? = null
        var targetX: Int? = null
        var targetY: Int? = null

        if (args.has(1) && args.has(2))
        {
            targetX = args.asInt(1)
            targetY = args.asInt(2)
        }

        if (targetX != null && targetY != null)
        {
            if (args.has(3))
            {
                target = args.asPlayer(3)
            }
            else
            {
                target = sender as? Player
            }
        }
        else
        {
            if (args.has(1))
            {
                target = args.asPlayer(1)
            }
            else
            {
                target = sender as? Player
            }

            val (x, y) = this.findSpawnPoint(town)
            targetX = x
            targetY = y
        }

        args.ensureNotNull(target, "Nie znaleziono podanego gracza!")
        target!!

        args.ensureTrue({ targetX != null && targetY != null }, "Nie znaleziono miejsca do teleportacji na mapie ${args.asString(0)}")
        targetX!!
        targetY!!

        val location = Location(town, targetX, targetY)

        sender.sendMessage("Teleportuje ${target.name} do ${location.toSimpleString()}")
        sender.server.gameLogger.info("${sender.name}: .tp ${town.id}[${town.name}] $targetX $targetY ${target.name}")
        target.teleport(location)
    }

    private fun findSpawnPoint(town: TownImpl): Pair<Int?, Int?>
    {
        var targetX: Int? = null
        var targetY: Int? = null

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

        return Pair(targetX, targetY)
    }
}