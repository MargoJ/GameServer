package pl.margoj.server.implementation.player.sublisteners

import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.network.protocol.IncomingPacket
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.player.PlayerConnection
import pl.margoj.server.implementation.player.PlayerImpl

abstract class PlayerPacketSubListener(
        val connection: PlayerConnection,
        val onlyOnPlayer: Boolean = false,
        val onlyWithType: String? = null,
        val async: Boolean = false
)
{
    abstract fun handle(packet: IncomingPacket, out: OutgoingPacket, query: Map<String, String>): Boolean

    protected val player: PlayerImpl? get() = this.connection.player
    protected val server: ServerImpl get() = this.connection.manager.server

    protected fun checkForMaliciousData(condition: Boolean, info: String)
    {
        if (condition)
        {
            this.reportMaliciousData(info)
        }
    }

    protected fun reportMaliciousData(info: String): Nothing
    {
        this.connection.disconnect("Malicious packet")
        throw IllegalArgumentException("Malicius packet: " + info)
    }

    companion object
    {
        val DEFAULTS = mutableListOf<(PlayerConnection) -> PlayerPacketSubListener>(
                // sync
                ::PlayerInitLvlCheckListener,
                ::PlayerEventCheckPacketListener,
                ::PlayerPreMovementPacketListener,
                ::PlayerInventoryPacketListener,
                ::PlayerChatAndConsolePacketListener,
                ::PlayerAddStatPointListener,
                ::PlayerLogoutListener,
                ::PlayerTalkListener,
                ::PlayerBattleListener,
                ::PlayerBattleSendListener,
                ::AdditionalPlayerPacketListener,

                // async
                ::PlayerInitListener,
                ::PlayerMovementSendListener,
                ::PlayerEventPacketListener,
                ::PlayerModifierPacketListener
        )
    }
}