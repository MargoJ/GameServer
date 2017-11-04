package pl.margoj.server.implementation.battle

import pl.margoj.mrf.item.ItemCategory
import pl.margoj.mrf.item.ItemProperties
import pl.margoj.server.api.battle.BattleTeam
import pl.margoj.server.api.inventory.ItemStack
import pl.margoj.server.api.player.Player
import pl.margoj.server.api.player.Profession
import pl.margoj.server.implementation.battle.buff.Buff
import pl.margoj.server.implementation.entity.LivingEntityImpl
import pl.margoj.server.implementation.item.ItemStackImpl
import pl.margoj.server.implementation.network.protocol.jsons.BattleParticipant
import pl.margoj.server.implementation.npc.Npc

class BattleData(val entity: LivingEntityImpl, val battle: BattleImpl, val team: BattleTeam)
{
    init
    {
        this.reset()
    }

    /** in-battle id */
    val id = if (this.entity is Player) this.entity.id else (-(this.entity as Npc).id)

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
            if (value != field)
            {
                field = value
                BattleImpl.logger.trace("${this.battle.battleId}: SA change: $field -> $value")

                this.battle.updateAttackSpeedThreshold()
            }
        }

    /** turn attack speed, used to calculate whose turn is now */
    var turnAttackSpeed = 0.0

    /** health to be shown in the battle */
    val healthPercent: Int
        get() = if (this.dead) 0 else this.entity.healthPercent

    private var buffs_: HashMap<Class<*>, Buff> = hashMapOf()
    val buffs: Collection<Buff> get() = this.buffs_.values

    fun reset()
    {
        this.initialized = false
        this.lastLog = -1
        this.needsAutoUpdate = true
        this.lastUpdateSendTick = -1L
    }

    fun updatedNow()
    {
        this.lastUpdatedTick = this.entity.server.ticker.currentTick
    }

    fun createBattleParticipantObject(target: LivingEntityImpl): BattleParticipant
    {
        val obj = BattleParticipant(this.id.toLong())

        if (!target.battleData!!.initialized)
        {
            obj.name = entity.name
            obj.level = entity.level
            obj.profession = entity.stats.profession
            obj.npc = if (entity is Npc) 1 else 0
            obj.gender = entity.gender.id.toString()
            obj.team = if (this.team == BattleTeam.TEAM_A) 1 else 2
            obj.icon = this.entity.icon
        }

        obj.healthPercent = if (entity.battleData!!.dead) 0 else entity.healthPercent
        obj.row = this.row
        obj.buffs = 0

        for ((_, buff) in this.buffs_)
        {
            obj.buffs = obj.buffs!! or buff.margoId
        }

        if (target.battleData!!.team == this.team)
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

        if (this.team == BattleTeam.TEAM_A)
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

    fun addBuff(buff: Buff, turns: Int)
    {
        BattleImpl.logger.trace("${this.battle.battleId}: ${this.entity}: adding buff $buff for $turns turns")

        buff.activeUntil = this.battle.currentTurn + turns
        this.buffs_.put(buff.javaClass, buff)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Buff> getBuff(`class`: Class<T>): T?
    {
        return this.buffs_[`class`] as T?
    }

    fun removeBuff(buff: Buff)
    {
        BattleImpl.logger.trace("${this.battle.battleId}: ${this.entity}: removing buff $buff")

        this.buffs_.values.remove(buff)
    }

    private fun getItemType(item: ItemStack?): ItemCategory?
    {
        return (item as? ItemStackImpl)?.item?.margoItem?.get(ItemProperties.CATEGORY)
    }
}