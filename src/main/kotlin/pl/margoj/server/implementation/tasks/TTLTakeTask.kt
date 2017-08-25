package pl.margoj.server.implementation.tasks

import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.player.StatisticType
import java.util.concurrent.TimeUnit

class TTLTakeTask(val server: ServerImpl) : Runnable
{
    override fun run()
    {
        for (player in server.players)
        {
            if (player.data.lastTtlPointTaken == -1L)
            {
                player.data.lastTtlPointTaken = System.currentTimeMillis()
                continue
            }

            if (System.currentTimeMillis() > player.data.lastTtlPointTaken + TimeUnit.MINUTES.toMillis(1))
            {
                if (player.location.town!!.isTown || player.location.town!!.parentMap?.isTown == true)
                {
                    continue
                }

                player.data.lastTtlPointTaken = System.currentTimeMillis()

                if (player.data.ttl == 0)
                {
                    continue
                }
                if (player.data.ttl < 0)
                {
                    player.server.gameLogger.warn("${player.name}: ujemne wyczerpanie ${player.data.ttl} ! przywracam do 0, lokacja=${player.location.toSimpleString()}")
                    player.data.ttl = 0
                }
                else if (player.data.ttl > 0)
                {
                    player.server.gameLogger.info("${player.name}: spadek wyczerpania ${player.data.ttl} -> ${player.data.ttl - 1}, lokacja=${player.location.toSimpleString()}")
                    player.data.ttl--
                }

                player.connection.addModifier { it.addStatisticRecalculation(StatisticType.TTL) }
            }
        }
    }
}