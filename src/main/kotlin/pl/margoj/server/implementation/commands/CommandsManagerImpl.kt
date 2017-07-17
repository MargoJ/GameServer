package pl.margoj.server.implementation.commands

import org.apache.commons.lang3.StringUtils
import pl.margoj.server.api.commands.CommandSender
import pl.margoj.server.api.commands.CommandsManager
import pl.margoj.server.api.inventory.ItemSlot
import pl.margoj.server.api.map.Location
import pl.margoj.server.api.player.Player
import pl.margoj.server.api.utils.Parse
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.item.ItemImpl
import pl.margoj.server.implementation.item.ItemLocation
import pl.margoj.server.implementation.player.PlayerImpl
import java.util.Arrays
import java.util.stream.Collectors

class CommandsManagerImpl(val server: ServerImpl) : CommandsManager
{
    override fun dispatchCommand(sender: CommandSender, string: String)
    {
        var input = string.trim()
        if (input.first() == '.' || input.first() == '/')
        {
            input = input.substring(1)
        }
        val parts = StringUtils.split(input, ' ')
        val command = parts[0]
        val args = parts.copyOfRange(1, parts.size)
        commandDispatched(sender, command, args)
    }

    private fun commandDispatched(sender: CommandSender, command: String, args: Array<String>)
    {
        // TODO: TEMP
        val player = sender as Player
        sender.logToConsole("cmd: $command, args = ${Arrays.toString(args)}", Player.ConsoleMessageSeverity.WARN)

        when (command.toLowerCase())
        {
            "help" ->
            {
                player.logToConsole("Dostępne komendy: <br>" +
                        " - .help - wyświetla help<br>" +
                        " - .towns - wyświetla dostępne mapy<br>" +
                        " - .tp &lt;mapa> [x] [y] - teleportuje na wybraną mape<br>" +
                        " - .senditem [item] = wyswietla wszystkie przedmioty lub wysyla pakiet z przedmiotem"
                )
            }
            "towns" ->
            {
                player.logToConsole("Dostępne mapy: <br>" + server.towns.stream().map { " - ${it.id} <br>" }.collect(Collectors.joining()))
            }
            "tp" ->
            {
                if (args.isEmpty())
                {
                    player.logToConsole("Prawidłowe użycie: .tp &lt;mapa> [x] [y] - teleportuje na wybraną mape", Player.ConsoleMessageSeverity.ERROR)
                    return
                }

                val town = player.server.getTownById(args[0])
                if (town == null)
                {
                    player.logToConsole("Nie znaleziono mapy: " + args[0], Player.ConsoleMessageSeverity.ERROR)
                    return
                }

                var targetX: Int? = null
                var targetY: Int? = null

                if (args.size >= 3)
                {
                    targetX = Parse.parseInt(args[1])
                    targetY = Parse.parseInt(args[2])
                }

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

                if (targetX == null || targetY == null)
                {
                    player.logToConsole("Nie znaleziono miejsca do teleportacji na mapie " + args[0], Player.ConsoleMessageSeverity.ERROR)
                    return
                }

                player.logToConsole("Teleportuje...")
                player.teleport(Location(town, targetX, targetY))
            }
            "senditem" ->
            {
                if (args.isEmpty())
                {
                    player.logToConsole("Dostępne przedmioty: <br> " + this.server.items.stream().map { "${it.margoItem.id} [${it.margoItem.name}]<br>" }.collect(Collectors.joining()))
                    return
                }

                val item = player.server.getItemById(args[0]) as? ItemImpl?
                if (item == null)
                {
                    player.logToConsole("Nie znaleziono przedmiotu: " + args[0], Player.ConsoleMessageSeverity.ERROR)
                    return
                }

                val itemStack = this.server.itemManager.newItemStack(item)
                val packet = this.server.itemManager.createItemObject(itemStack)

                packet.own = player.id
                packet.location = ItemLocation.PLAYERS_INVENTORY.margoType
                packet.x = 0
                packet.y = 0
                packet.slot = ItemSlot.DEFAULT.margoId

                player as PlayerImpl
                player.connection.addModifier { it.addItem(packet) }

                player.logToConsole("Pakiet przedmiotu dodany do kolejki wyslania!", Player.ConsoleMessageSeverity.WARN)
            }
        }
    }
}