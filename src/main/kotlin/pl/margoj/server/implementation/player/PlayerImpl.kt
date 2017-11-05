package pl.margoj.server.implementation.player

import pl.margoj.server.api.battle.BattleStats
import pl.margoj.server.api.battle.BattleUnableToStartException
import pl.margoj.server.api.battle.DamageSource
import pl.margoj.server.api.chat.ChatMessage
import pl.margoj.server.api.commands.CommandSender
import pl.margoj.server.api.events.player.PlayerQuitEvent
import pl.margoj.server.api.map.Location
import pl.margoj.server.api.map.Town
import pl.margoj.server.api.player.Gender
import pl.margoj.server.api.player.Player
import pl.margoj.server.api.player.PlayerRank
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.battle.BattleData
import pl.margoj.server.implementation.entity.*
import pl.margoj.server.implementation.entity.simple.GravestoneEntity
import pl.margoj.server.implementation.inventory.AbstractInventoryImpl
import pl.margoj.server.implementation.inventory.map.MapInventoryImpl
import pl.margoj.server.implementation.inventory.player.ItemTracker
import pl.margoj.server.implementation.inventory.player.PlayerInventoryImpl
import pl.margoj.server.implementation.map.TownImpl
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.network.protocol.jsons.OtherObject
import pl.margoj.server.implementation.npc.NpcTalk
import java.util.Collections
import java.util.Date
import java.util.LinkedList

class PlayerImpl(override val data: PlayerDataImpl, override val server: ServerImpl, val connection: PlayerConnection) : LivingEntityImpl(), Player
{
    override val id: Int = this.data.id.toInt()

    override val name: String = this.data.characterName

    override val level: Int get() = this.data.level

    override val gender: Gender get() = this.data.gender

    override val icon: String get() = this.data.icon

    override val deadUntil: Date? get() = this.data.deadUntil

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

    override val rank: PlayerRank get() = this.connection.authSession.rank

    var currentNpcTalk: NpcTalk? = null

    val possibleInventorySources by lazy { arrayListOf<AbstractInventoryImpl>() }

    val entityTracker = EntityTracker(this)

    val itemTracker = ItemTracker(this)

    internal var initialized = false

    override val battleUnavailabilityCause: BattleUnableToStartException.Cause?
        get()
        {
            if (!this.online)
            {
                return BattleUnableToStartException.Cause.PLAYER_IS_OFFLINE
            }

            return super.battleUnavailabilityCause
        }

    override val withGroup: List<LivingEntityImpl>
        get()
        {
            return Collections.singletonList(this) // TODO
        }

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

    override var battleData: BattleData?
        get() = super.battleData
        set(value)
        {
            this.currentNpcTalk = null
            super.battleData = value
        }

    override val canAnnounce: Boolean
        get() = super.canAnnounce && this.online

    val chatHistory = LinkedList<ChatMessage>()

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
        this.chatHistory.add(message)
        if (this.chatHistory.size > 100)
        {
            this.chatHistory.removeFirst()
        }

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
            this.sendChatMessage(ChatMessage(text = location.town!!.name, style = "entertown"))
        }
        else
        {
            location.copyValuesTo(current)
            this.movementManager.clearQueue()
            this.movementManager.updatePosition()
        }
    }

    override fun teleport(map: Town)
    {
        this.teleport(map.spawnPoint)
    }

    override fun kill(damageSource: DamageSource?)
    {
        super.kill(damageSource)
        this.data.deadUntil = Date(System.currentTimeMillis() + this.killTime)
        this.data.hp = 1

        val gravestone = GravestoneEntity(this.server, this.location.copyImmutable(), this, "Zabity przez: ${damageSource?.damageSourceName ?: "???"}", System.currentTimeMillis(), "")
        this.server.entityManager.registerEntity(gravestone)

        val respawnLocation = this.location.town?.respawnMap as? TownImpl ?: return
        val spawnPoint = respawnLocation.cachedMapData.spawnPoint

        this.teleport(spawnPoint)
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

        this.server.gameLogger.info("${this.name}: zalogowano się do gry. IP: ${this.connection.ip}. Pozycja: ${this.location.toSimpleString()}")
    }

    fun disconnect()
    {
        this.connection.dispose()
        this.online = false

        this.server.ticker.unregisterTickable(this.itemTracker)
        this.server.entityManager.unregisterEntity(this)
        this.server.eventManager.call(PlayerQuitEvent(this))

        this.server.gameLogger.info("${this.name}: wylogowano się z gry. Pozycja: ${this.location.toSimpleString()}")
    }

    override fun announce(tracker: EntityTracker, out: OutgoingPacket)
    {
        val other = OtherObject()
        other.id = this.id
        other.nick = this.name
        other.icon = this.data.icon
        other.clan = "" // TODO
        other.x = this.location.x
        other.y = this.location.y
        other.direction = this.movementManager.playerDirection
        other.rights = when (this.rank)
        {
            PlayerRank.ADMINISTRATOR -> 1
            PlayerRank.SUPER_GAME_MASTER -> 16
            PlayerRank.GAME_MASTER -> 2
            PlayerRank.USER -> 0
        }
        other.level = this.data.level
        other.profession = this.data.profession
        other.attributes = 0 // TODO
        other.relation = "" // TODO

        this.setTrackingData(tracker, TrackingData(this))

        out.addOther(other)
    }

    override fun dispose(tracker: EntityTracker, out: OutgoingPacket)
    {
        this.setTrackingData(tracker, null)

        val other = OtherObject()
        other.id = this.id
        other.del = 1

        out.addOther(other)
    }

    override fun update(tracker: EntityTracker, out: OutgoingPacket)
    {
        val trackingData = this.getTrackingData(tracker)
        var updated = false
        val other = OtherObject()
        other.id = this.id

        val location = this.location

        if (trackingData.x != location.x || trackingData.y != location.y)
        {
            updated = true
            other.x = location.x
            other.y = location.y
            trackingData.x = location.x
            trackingData.y = location.y
        }

        if (trackingData.lastDirection != this.direction)
        {
            updated = true
            other.direction = this.direction
            trackingData.lastDirection = this.direction
        }

        if (trackingData.lastProfession != this.stats.profession)
        {
            updated = true
            other.profession = this.stats.profession
            trackingData.lastProfession = this.stats.profession
        }

        if (trackingData.lastLevel != this.level)
        {
            updated = true
            other.level = this.level
            trackingData.lastLevel = this.level
        }

        if (updated)
        {
            out.addOther(other)
        }
    }

    override fun toString(): String
    {
        return "PlayerImpl(id=$id, name='$name')"
    }
}