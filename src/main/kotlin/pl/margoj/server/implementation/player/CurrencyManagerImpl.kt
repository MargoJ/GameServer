package pl.margoj.server.implementation.player

import pl.margoj.server.api.player.CurrencyManager

class CurrencyManagerImpl(val player: PlayerImpl): CurrencyManager
{
    override val goldLimit: Long = 0

    override val gold: Long = 0
}