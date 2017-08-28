package pl.margoj.server.implementation.player

import pl.margoj.server.api.battle.BattleStats
import pl.margoj.server.api.chat.ChatMessage
import pl.margoj.server.api.commands.CommandSender
import pl.margoj.server.api.events.player.PlayerQuitEvent
import pl.margoj.server.api.map.Location
import pl.margoj.server.api.player.Gender
import pl.margoj.server.api.player.Player
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.entity.EntityImpl
import pl.margoj.server.implementation.entity.EntityTracker
import pl.margoj.server.implementation.inventory.AbstractInventoryImpl
import pl.margoj.server.implementation.inventory.map.MapInventoryImpl
import pl.margoj.server.implementation.inventory.player.ItemTracker
import pl.margoj.server.implementation.inventory.player.PlayerInventoryImpl
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.npc.NpcTalk

class PlayerImpl(override val data: PlayerDataImpl, override val server: ServerImpl, val connection: PlayerConnection) : EntityImpl(data.id.toInt()), Player
{
    override val id: Int = this.data.id.toInt()

    override val name: String = this.data.characterName

    override val level: Int get() = this.data.level

    override val gender: Gender get() = this.data.gender

    override val icon: String get() = this.data.icon

    override val location: Location get() = this.movementManager.location

    override val direction: Int get() = this.movementManager.playerDirection

    override val inventory: PlayerInventoryImpl get() = this.data.inventory!!

    override val stats: BattleStats get() = this.data

    override var online: Boolean = false
        private set

    override val movementManager = MovementManagerImpl(this)

    override val currencyManager = CurrencyManagerImpl(this)

    override var hp: Int
        get() = this.data.hp
        set(value)
        {
            this.data.hp = value
        }

    var currentNpcTalk: NpcTalk? = null

    val possibleInventorySources by lazy { arrayListOf<AbstractInventoryImpl>() }

    val entityTracker = EntityTracker(this)

    val itemTracker = ItemTracker(this)

    private var task: ((CommandSender) -> Unit)? = null

    val canBeLoggedOff: Boolean
        get()
        {
            if (this.currentBattle != null)
            {
                return this.currentBattle!!.finished
            }

            return true
        }

    override fun addConfirmationTask(task: (CommandSender) -> Unit, message: String)
    {
        this.sendMessage(message)
        this.task = task
    }

    override fun executeConfirmationTask(): Boolean
    {
        val result = task?.invoke(this) != null
        task = null
        return result
    }

    override fun sendMessage(message: String, messageSeverity: CommandSender.MessageSeverity)
    {
        this.logToConsole(message.replace("\n", "<br>"), messageSeverity)
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
            this.updatePossibleInventories()
        }
        else
        {
            location.copyValuesTo(current)
            this.movementManager.clearQueue()
            this.movementManager.updatePosition()
        }
    }

    fun updatePossibleInventories()
    {
        this.possibleInventorySources.clear()
        this.possibleInventorySources.add(this.inventory)
        if (this.location.town != null)
        {
            this.possibleInventorySources.add(this.location.town!!.inventory as MapInventoryImpl)
        }
    }

    fun recalculateWarriorStatistics()
    {
        this.connection.addModifier { it.addStatisticRecalculation(StatisticType.WARRIOR) }
    }

    fun connected()
    {
        this.updatePossibleInventories()
        this.server.ticker.registerTickable(this.itemTracker)
        this.online = true

        this.server.gameLogger.info("${this.name}: zalogowano się do gry. Pozycja: ${this.location.toSimpleString()}")
    }

    fun disconnect()
    {
        this.server.ticker.unregisterTickable(this.itemTracker)
        this.connection.dispose()
        this.server.networkManager.resetPlayerConnection(this.connection)
        this.server.entityManager.unregisterEntity(this)
        this.online = false
        this.server.eventManager.call(PlayerQuitEvent(this))

        this.server.gameLogger.info("${this.name}: wylogowano się z gry. Pozycja: ${this.location.toSimpleString()}")
    }

    override fun toString(): String
    {
        return "PlayerImpl(id=$id, name='$name')"
    }
}