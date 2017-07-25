package pl.margoj.server.implementation.player

import pl.margoj.server.api.chat.ChatMessage
import pl.margoj.server.api.commands.CommandSender
import pl.margoj.server.api.events.player.PlayerQuitEvent
import pl.margoj.server.api.map.Location
import pl.margoj.server.api.player.Player
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.entity.EntityImpl
import pl.margoj.server.implementation.entity.EntityTracker
import pl.margoj.server.implementation.inventory.AbstractInventoryImpl
import pl.margoj.server.implementation.inventory.player.ItemTracker
import pl.margoj.server.implementation.inventory.player.PlayerInventoryImpl
import pl.margoj.server.implementation.network.protocol.OutgoingPacket

class PlayerImpl(override val data: PlayerDataImpl, override val server: ServerImpl, val connection: PlayerConnection) : EntityImpl(data.id.toInt()), Player
{
    override val id: Int = this.data.id.toInt()

    override val name: String = this.data.characterName

    override val location: Location get() = this.movementManager.location

    override val direction: Int get() = this.movementManager.playerDirection

    override val inventory: PlayerInventoryImpl get() = this.data.inventory!!

    override var online: Boolean = false
        private set

    override val movementManager = MovementManagerImpl(this)

    override val currencyManager = CurrencyManagerImpl(this)

    val possibleInventorySources = arrayListOf<AbstractInventoryImpl>(this.inventory)

    val entityTracker = EntityTracker(this)

    val itemTracker = ItemTracker(this)

    override fun sendMessage(message: String, messageSeverity: CommandSender.MessageSeverity)
    {
        this.logToConsole(message, messageSeverity)
    }

    override fun sendChatMessage(message: ChatMessage)
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

    override fun logToConsole(text: String, severity: CommandSender.MessageSeverity)
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
        this.online = true
    }

    fun disconnect()
    {
        this.server.ticker.unregisterTickable(this.itemTracker)
        this.connection.dispose()
        this.server.networkManager.resetPlayerConnection(this.connection)
        this.server.entityManager.unregisterEntity(this)
        this.online = false
        this.server.eventManager.call(PlayerQuitEvent(this))
    }

    override fun toString(): String
    {
        return "PlayerImpl(id=$id, name='$name')"
    }
}