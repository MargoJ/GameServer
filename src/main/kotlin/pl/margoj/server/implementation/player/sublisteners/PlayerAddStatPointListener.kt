package pl.margoj.server.implementation.player.sublisteners

import pl.margoj.server.implementation.network.protocol.IncomingPacket
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.player.PlayerConnection
import pl.margoj.server.implementation.player.StatisticType

class PlayerAddStatPointListener(connection: PlayerConnection) : PlayerPacketSubListener
(
        connection,
        onlyOnPlayer = true,
        onlyWithType = "useab"
)
{
    override fun handle(packet: IncomingPacket, out: OutgoingPacket, query: Map<String, String>): Boolean
    {
        val player = this.player!!
        player.server.gameLogger.info("${player.name}: levelowanie statystyki: ${query["a"]} o ${query["cnt"]}")

        val amount = query["cnt"]?.toInt()
        this.checkForMaliciousData(amount == null || amount <= 0, "invalid amount (cnt)")
        amount!!

        if (amount > player.data.statPoints)
        {
            return true
        }

        when (query["a"])
        {
            "str" -> player.data.baseStrength += amount
            "agi" -> player.data.baseAgility += amount
            "int" -> player.data.baseIntellect += amount
            else -> this.reportMaliciousData("invalid stat ${query["a"]}")
        }

        player.data.statPoints -= amount

        player.connection.addModifier { it.addStatisticRecalculation(StatisticType.WARRIOR) }

        return true
    }
}