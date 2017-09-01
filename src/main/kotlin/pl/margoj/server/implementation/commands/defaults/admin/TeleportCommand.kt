package pl.margoj.server.implementation.commands.defaults.admin

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

            targetX = town.cachedMapData.spawnPoint.x
            targetY = town.cachedMapData.spawnPoint.y
        }

        args.ensureNotNull(target, "Nie znaleziono podanego gracza!")
        target!!

        val location = Location(town, targetX, targetY)

        sender.sendMessage("Teleportuje ${target.name} do ${location.toSimpleString()}")
        sender.server.gameLogger.info("${sender.name}: .tp ${town.id}[${town.name}] $targetX $targetY ${target.name}")
        target.teleport(location)
    }
}