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

        val amount = query["cnt"]?.toInt()
        this.checkForMaliciousData(amount == null || amount <= 0, "invalid amount (cnt)")
        amount!!

        if (amount > player.data.statPoints)
        {
            return true
        }

        player.data.statPoints -= amount

        when (query["a"])
        {
            "str" -> player.data.baseStrength += amount
            "agi" -> player.data.baseAgility += amount
            "int" -> player.data.baseIntellect += amount
            else -> this.reportMaliciousData("invalid stat ${query["a"]}")
        }

        player.connection.addModifier { it.addStatisticRecalculation(StatisticType.WARRIOR) }

        return true
    }
}