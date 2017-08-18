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
                // TODO: CHECK IF IN TOWN

                player.data.lastTtlPointTaken = System.currentTimeMillis()

                if(player.data.ttl == 0)
                {
                    continue
                }
                if (player.data.ttl < 0)
                {
                    player.data.ttl = 0
                }
                else if (player.data.ttl > 0)
                {
                    player.data.ttl--
                }

                player.connection.addModifier { it.addStatisticRecalculation(StatisticType.TTL) }
            }
        }
    }
}