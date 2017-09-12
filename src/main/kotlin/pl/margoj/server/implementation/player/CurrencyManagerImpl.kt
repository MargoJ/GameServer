package pl.margoj.server.implementation.player

import pl.margoj.server.api.player.CurrencyManager

class CurrencyManagerImpl(val player: PlayerImpl) : CurrencyManager
{
    override val goldLimit: Long = 3_000_000_000L // TODO

    override var gold: Long
        set(value)
        {
            player.data.gold = Math.min(value, goldLimit)
            this.requestCurrencyRecalculation()
        }
        get()
        {
            return player.data.gold
        }

    override fun canFit(gold: Long): Boolean
    {
        val targetGold = this.gold + gold
        return targetGold >= 0 && targetGold <= this.goldLimit
    }

    fun requestCurrencyRecalculation()
    {
        this.player.connection.addModifier { it.addStatisticRecalculation(StatisticType.CURRENCY) }
    }

    override fun giveGold(gold: Long)
    {
        if (gold == 0L)
        {
            return
        }

        this.gold += gold

        if (gold > 0)
        {
            this.player.displayScreenMessage("Otrzymano $gold złotych monet")
        }
        else
        {
            this.player.displayScreenMessage("Stracono ${-gold} złotych monet")
        }
    }
}