package pl.margoj.server.implementation.player

import pl.margoj.server.api.player.CurrencyManager

class CurrencyManagerImpl(val player: PlayerImpl) : CurrencyManager
{
    override val goldLimit: Long = 3_000_000_000L // TODO

    override var gold: Long
        set(value)
        {
            if (!this.canFit(value))
            {
                throw IllegalStateException("can't fit gold, ${this.gold} -> $value")
            }
            this.requestCurrencyRecalculation()
            player.data.gold = value
        }
        get()
        {
            return player.data.gold
        }

    override fun canFit(gold: Long): Boolean
    {
        return gold >= 0 && gold <= this.goldLimit
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
            this.player.displayScreenMessage("Stracono $gold złotych monet")
        }
    }
}