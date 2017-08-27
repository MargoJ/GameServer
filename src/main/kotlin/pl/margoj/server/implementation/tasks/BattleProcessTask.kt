package pl.margoj.server.implementation.tasks

import pl.margoj.server.implementation.ServerImpl

class BattleProcessTask(val server: ServerImpl) : Runnable
{
    override fun run()
    {
        val currentTick = server.ticker.currentTick

        for (player in server.players)
        {
            val battle = player.currentBattle ?: continue

            if (battle.finished || !battle.started || currentTick <= battle.lastProcessTick)
            {
                continue
            }

            battle.processTurn()
            battle.lastProcessTick = currentTick
        }
    }
}