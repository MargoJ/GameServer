package pl.margoj.server.implementation.player

import pl.margoj.server.api.chat.ChatMessage
import pl.margoj.server.api.map.Location
import pl.margoj.server.api.player.Player
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.entity.EntityImpl
import pl.margoj.server.implementation.entity.EntityTracker
import pl.margoj.server.implementation.inventory.AbstractInventoryImpl
import pl.margoj.server.implementation.inventory.player.ItemTracker
import pl.margoj.server.implementation.inventory.player.PlayerInventoryImpl
import pl.margoj.server.implementation.network.protocol.OutgoingPacket

class PlayerImpl(override val id: Int, override val name: String, override val server: ServerImpl, val connection: PlayerConnection) : EntityImpl(id), Player
{
    override val location: Location get() = this.movementManager.location

    override val direction: Int get() = this.movementManager.playerDirection

    override val movementManager = MovementManagerImpl(this)

    override val currencyManager = CurrencyManagerImpl(this)

    override val data = PlayerDataImpl(this)

    override val inventory = PlayerInventoryImpl(this)

    val possibleInventorySources = arrayListOf<AbstractInventoryImpl>(this.inventory)

    val entityTracker = EntityTracker(this)

    val itemTracker = ItemTracker(this)

    override fun sendMessage(message: ChatMessage)
    {
        this.connection.addModifier { it.addChatMessage(message) }
    }

    override fun displayAlert(alert: String)
    {
        this.connection.addModifier { it.addAlert(alert) }
    }

    override fun displayScreenMessage(message: String)
    {
        this.connection.addModifier { it.addScreenMessage(message) }
    }

    override fun logToConsole(text: String, severity: Player.ConsoleMessageSeverity)
    {
        this.connection.addModifier { it.addLogMessage(text, severity) }
    }

    override fun teleport(location: Location)
    {
        val current = this.movementManager.location

        if (current.town != location.town)
        {
            location.copyValuesTo(current)
            this.connection.addModifier { it.addEngineAction(OutgoingPacket.EngineAction.RELOAD) }
        }
        else
        {
            location.copyValuesTo(current)
            this.movementManager.clearQueue()
            this.movementManager.updatePosition()
        }
    }

    fun connected()
    {
        this.server.ticker.registerTickable(this.itemTracker)
    }

    fun disconnected()
    {
        this.server.ticker.unregisterTickable(this.itemTracker)
    }
}