package pl.margoj.server.implementation.commands.defaults

import pl.margoj.server.api.commands.Arguments
import pl.margoj.server.api.commands.CommandListener
import pl.margoj.server.api.commands.CommandSender
import pl.margoj.server.api.utils.TimeFormatUtils
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.database.TableNames
import pl.margoj.server.implementation.player.PlayerImpl
import java.util.Date

class KillCommand(server: ServerImpl) : CommandListener
{
    override fun commandPerformed(command: String, sender: CommandSender, args: Arguments)
    {
        args.ensureTrue({ args.has(1) }, "Prawidłowe użycie: .kill &lt;gracz> &lt;czas>")

        val player = args.asPlayer(0) as? PlayerImpl?
        val time = TimeFormatUtils.parseTime(args.asString(1))

        if (player != null)
        {
            player.data.deadUntil = Date(System.currentTimeMillis() + time)
            success(sender, player.name, time)
        }
        else
        {
            val playerName = args.asString(0)
            sender.sendMessage("Szukam gracza ${playerName}...")

            val databaseManager = (sender.server as ServerImpl).databaseManager

            // find in cache
            val cacheEntry = databaseManager.playerDataCache.cachedEntries.find { it?.characterName == playerName }
            if (cacheEntry != null)
            {
                cacheEntry.deadUntil = Date(System.currentTimeMillis() + time)
            }

            // find in db
            sender.server.ticker.runAsync(Runnable {
                databaseManager.withConnection {
                    val killStatement = it.prepareStatement("UPDATE `${TableNames.PLAYERS}` SET `dead_until`=? WHERE `characterName`=?")
                    killStatement.setTimestamp(1, java.sql.Timestamp(System.currentTimeMillis() + time))
                    killStatement.setString(2, playerName)

                    if (killStatement.executeUpdate() <= 0)
                    {
                        sender.sendMessage("Nie odnaleziono gracza ${playerName} w bazie danych!", CommandSender.MessageSeverity.ERROR)
                    }
                    else
                    {
                        success(sender, playerName, time)
                    }
                }
            })
        }
    }

    private fun success(sender: CommandSender, player: String, time: Long)
    {
        val readableTime = TimeFormatUtils.getReadableTime(time)
        sender.sendMessage("Nałożono killa na gracza $player na $readableTime")
        sender.server.gameLogger.warn("${sender.name}: .kill $player $readableTime")
    }
}