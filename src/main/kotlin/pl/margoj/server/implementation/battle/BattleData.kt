package pl.margoj.server.implementation.battle

import pl.margoj.mrf.item.ItemCategory
import pl.margoj.mrf.item.ItemProperties
import pl.margoj.server.api.inventory.ItemStack
import pl.margoj.server.api.player.Player
import pl.margoj.server.api.player.Profession
import pl.margoj.server.implementation.entity.EntityImpl
import pl.margoj.server.implementation.item.ItemStackImpl
import pl.margoj.server.implementation.network.protocol.jsons.BattleParticipant
import pl.margoj.server.implementation.npc.Npc

class BattleData(val entity: EntityImpl, val battle: Battle, val team: Team)
{
    init
    {
        this.reset()
    }

    /* log */
    var log: MutableMap<Int, String> = hashMapOf()
    var logCount = 0
        private set

    /** in-battle id */
    val id = (if (this.entity is Player) this.entity.id else -this.entity.id).toLong()

    /** did player press the quit button? */
    var quitRequested: Boolean = false

    /** got init packet */
    var initialized = false

    /** last received log id */
    var lastLog: Int = -1

    /** auto-fight enabled **/
    var auto: Boolean = false

    /** send update for auto var? */
    var needsAutoUpdate = true

    /** last time when a secondLeft has been decreased */
    var lastSecondUpdate = 0L

    /** how much second did player have to make a turn */
    var startsMove: Int = 15

    /** how much seconds is there left for current turn */
    var secondsLeft = 0

    /** when a participant update was sent? */
    var lastUpdateSendTick = -1L

    /** when this participant was updated last time? */
    var lastUpdatedTick: Long = -1L

    /** row where participant is standing **/
    var row: Int = 0

    /** in-battle energy */
    var energy: Int = 0

    /** in-battle mana */
    var mana: Int = 0

    /** is this participant dead? */
    var dead = false

    /** in-battle attack speed */
    var battleAttackSpeed: Double = entity.stats.attackSpeed + 1.0
        set(value)
        {
            this.battle.updateAttackSpeedThreshold()
        }

    /** turn attack speed, used to calculate whose turn is now */
    var turnAttackSpeed = 0.0

    fun reset()
    {
        this.initialized = false
        this.lastLog = -1
        this.needsAutoUpdate = true
        this.lastUpdateSendTick = -1L
    }

    fun addLog(log: String)
    {
        this.log.put(this.logCount++, log)
    }

    fun updatedNow()
    {
        this.lastUpdatedTick = this.entity.server.ticker.currentTick
    }

    fun createBattleParticipantObject(target: EntityImpl): BattleParticipant
    {
        val obj = BattleParticipant(this.id)

        if (!target.battleData!!.initialized)
        {
            obj.name = entity.name
            obj.level = entity.level
            obj.profession = entity.stats.profession
            obj.npc = if (entity is Npc) 1 else 0
            obj.gender = entity.gender.id.toString()
            obj.team = if (this.team == Team.TEAM_A) 1 else 2
            obj.icon = this.entity.icon
        }

        obj.healthPercent = if (entity.battleData!!.dead) 0 else entity.healthPercent
        obj.row = this.row
        obj.buffs = 0 // TODO

        if (target == this.entity)
        {
            obj.energy = this.energy
            obj.mana = this.mana
            obj.fast = if (this.auto) 1 else 0
        }

        return obj
    }

    fun canReach(targetRow: Int): Boolean
    {
        if (this.hasRangedWeapon())
        {
            return true
        }

        if (this.team == Team.TEAM_A)
        {
            return this.row >= targetRow - 1
        }
        else
        {
            return this.row <= targetRow + 1
        }
    }

    fun hasRangedWeapon(): Boolean
    {
        return when (this.entity)
        {
            is Npc ->
            {
                when (this.entity.stats.profession)
                {
                    Profession.MAGE, Profession.HUNTER, Profession.TRACKER -> true
                    else -> false
                }
            }
            is Player ->
            {
                when (getItemType(this.entity.inventory.equipment.weapon))
                {
                    ItemCategory.WANDS, ItemCategory.STAFF -> true
                    ItemCategory.RANGE_WEAPON -> getItemType(this.entity.inventory.equipment.helper) == ItemCategory.ARROWS
                    else -> false
                }
            }
            else -> false
        }
    }

    private fun getItemType(item: ItemStack?): ItemCategory?
    {
        return (item as? ItemStackImpl)?.item?.margoItem?.get(ItemProperties.CATEGORY)
    }

    enum class Team
    {
        TEAM_A,
        TEAM_B
    }
}