package pl.margoj.server.implementation.player

import pl.margoj.mrf.item.ItemProperties
import pl.margoj.server.api.events.player.PlayerExpChangeEvent
import pl.margoj.server.api.events.player.PlayerLevelUpEvent
import pl.margoj.server.api.map.Location
import pl.margoj.server.api.player.Gender
import pl.margoj.server.api.player.PlayerData
import pl.margoj.server.api.player.Profession
import pl.margoj.server.api.utils.fastPow4
import pl.margoj.server.api.utils.floor
import pl.margoj.server.api.utils.pow
import pl.margoj.server.api.utils.toBigDecimal
import pl.margoj.server.implementation.inventory.player.PlayerInventoryImpl
import pl.margoj.server.implementation.item.ItemStackImpl
import pl.margoj.server.implementation.network.protocol.jsons.HeroObject
import java.util.Date

// TODO
class PlayerDataImpl(val id: Long, val characterName: String) : PlayerData
{
    var player_: PlayerImpl? = null
    val player: PlayerImpl get() = this.player_!!

    override var icon: String = "/vip/3599584.gif"
    override var profession: Profession = Profession.WARRIOR
    override var gender: Gender = Gender.MALE

    override var level: Int = 1
    override var xp: Long = 0

    override var statPoints: Int = 0
    override var baseStrength: Int = 4
    override var baseAgility: Int = 3
    override var baseIntellect: Int = 3

    override var strength: Int = 4
    override var agility: Int = 3
    override var intellect: Int = 3
    override var attackSpeed: Double = 1.00
    override var damage: IntRange = 0..0

    override var maxHp: Int = 0
    override var hp: Int = 0
        set(value)
        {
            field = Math.min(value, this.maxHp)
            this.player_?.recalculateWarriorStatistics()
        }
        get()
        {
            if(field > this.maxHp)
            {
                field = this.maxHp
            }

            return field
        }

    override var ttl: Int = 0
    var lastTtlPointTaken = -1L
    override var deadUntil: Date? = null
    override val isDead: Boolean get() = this.player.isDead

    var location: Location = Location(null)
    var inventory: PlayerInventoryImpl? = null

    /* use CurrencyManager to manipulate this value */
    internal var gold: Long = 0L

    override fun addExp(xp: Long)
    {
        val oldXp = this.xp
        val newXp = this.xp + xp

        val event = PlayerExpChangeEvent(this.player, oldXp, newXp, xp)
        this.player.server.eventManager.call(event)
        if (event.cancelled)
        {
            return
        }

        this.xp = event.newXp

        while (true)
        {
            val expToNextLevel = this.level.toLong().fastPow4() + 10

            if (this.xp < expToNextLevel || expToNextLevel < 0)
            {
                break
            }

            val oldLevel = this.level
            this.level++
            val newLevel = this.level

            this.onLevelUp(oldLevel, newLevel)
        }

        this.player.connection.addModifier { it.addStatisticRecalculation(StatisticType.WARRIOR) }
    }

    private fun onLevelUp(oldLevel: Int, newLevel: Int)
    {
        val connection = this.player.connection
        connection.addModifier { it.addScreenMessage("Awansowałeś na poziom $newLevel") }

        if (newLevel < 25)
        {
            val (bonusStrength, bonusAgility, bonusIntellect) = when (this.profession)
            {
                Profession.WARRIOR -> Triple(4, 1, 0)
                Profession.PALADIN -> if (newLevel % 2 == 0) Triple(2, 1, 2) else Triple(3, 0, 2)
                Profession.MAGE -> Triple(1, 1, 3)
                Profession.TRACKER -> Triple(1, 2, 2)
                Profession.HUNTER -> Triple(1, 4, 0)
                Profession.BLADE_DANCER -> Triple(3, 2, 0)
            }

            if (bonusStrength != 0)
            {
                connection.addModifier { it.addScreenMessage("+$bonusStrength siły") }
                this.baseStrength += bonusStrength
            }

            if (bonusAgility != 0)
            {
                connection.addModifier { it.addScreenMessage("+$bonusAgility zręcznośći") }
                this.baseAgility += bonusAgility
            }

            if (bonusIntellect != 0)
            {
                connection.addModifier { it.addScreenMessage("+$bonusIntellect intelektu") }
                this.baseIntellect += bonusIntellect
            }
        }
        else
        {
            statPoints++
        }

        connection.addModifier { it.addStatisticRecalculation(StatisticType.WARRIOR) }

        this.player.server.eventManager.call(PlayerLevelUpEvent(this.player, oldLevel, newLevel))
    }

    fun recalculateStatistics(type: StatisticType): HeroObject
    {
        val out = HeroObject()

        if (StatisticType.WARRIOR in type)
        {
            out.id = this.player.connection.aid
            out.blockade = 0
            out.permissions = 1
            out.nick = this.player.name
            out.prof = this.profession.id
            out.opt = 0

            // base
            this.strength = this.baseStrength
            this.agility = this.baseAgility
            this.intellect = this.baseIntellect

            // items base
            this.player.inventory.equipment.allItems.stream().filter { it != null }.forEach {
                val item = (it as ItemStackImpl).item.margoItem
                val all = item[ItemProperties.ALL_ATTRIBUTES]

                this.strength += item[ItemProperties.STRENGTH] + all
                this.agility += item[ItemProperties.AGILITY] + all
                this.intellect += item[ItemProperties.INTELLECT] + all
            }

            // max xp
            this.maxHp = (20 * (this.level.toDouble() pow 1.25) + this.strength * 5).floor()

            // attack speed
            this.attackSpeed = 1.00
            val baseASAgilityMultiplier = Math.min(this.agility, 100)
            val additionalSAAgilityMultiplier = Math.max(0, this.agility - 100) / 10
            this.attackSpeed += (baseASAgilityMultiplier + additionalSAAgilityMultiplier).toDouble() * 0.02

            // items rest
            this.damage = 0..0
            var hasDamage = false

            this.player.inventory.equipment.allItems.stream().filter { it != null }.forEach {
                val item = (it as ItemStackImpl).item.margoItem
                this.maxHp += item[ItemProperties.HEALTH]
                this.attackSpeed += (item[ItemProperties.ATTACK_SPEED].toDouble() / 100.0)
                this.maxHp += (this.strength * item[ItemProperties.HEALTH_FOR_STRENGTH]).toInt()

                // damage
                val thisDamage = item[ItemProperties.DAMAGE]
                if (thisDamage.first != 0 || thisDamage.endInclusive != 0)
                {
                    val thisDamageAvg = (thisDamage.first + thisDamage.endInclusive) / 2
                    val ourDamageAvg = (this.damage.first + this.damage.endInclusive) / 2

                    if (thisDamageAvg > ourDamageAvg)
                    {
                        this.damage = thisDamage
                        hasDamage = true
                    }
                }
            }

            if (!hasDamage)
            {
                this.damage = Math.round(this.baseStrength * 0.7).toInt()..Math.round(this.baseStrength * 0.8).toInt()
            }

            out.exp = this.xp
            out.lvl = this.level

            out.bagi = this.baseAgility
            out.bint = this.baseIntellect
            out.bstr = this.baseStrength
            out.ap = this.statPoints

            out.warriorStats.st = this.strength
            out.warriorStats.ag = this.agility
            out.warriorStats.it = this.intellect

            out.warriorStats.maxhp = this.maxHp
            out.warriorStats.hp = this.hp
            out.warriorStats.sa = this.attackSpeed.toBigDecimal()

            out.warriorStats.dmg = (this.damage.first + this.damage.endInclusive) / 2

            out.warriorStats.crit = 1.04.toBigDecimal()
            out.warriorStats.ac = 0
            out.warriorStats.resfire = 0
            out.warriorStats.resfrost = 0
            out.warriorStats.reslight = 0
            out.warriorStats.act = 0
            out.warriorStats.critmval = 1.20.toBigDecimal()
            out.warriorStats.critmval_f = 1.20.toBigDecimal()
            out.warriorStats.critmval_c = 1.20.toBigDecimal()
            out.warriorStats.critmval_l = 1.20.toBigDecimal()
            out.warriorStats.mana = 0
            out.warriorStats.block = 0
        }

        if (StatisticType.POSITION in type)
        {
            out.dir = this.player.movementManager.playerDirection
            out.x = this.player.location.x
            out.y = this.player.location.y

            if (this.player.movementManager.resetPosition)
            {
                this.player.movementManager.resetPosition = false
                out.back = 1
            }
        }

        if (StatisticType.CURRENCY in type)
        {
            out.credits = 0
            out.runes = 0
            out.honor = 0
            out.gold = this.player.currencyManager.gold
            out.goldlim = this.player.currencyManager.goldLimit
        }

        if (StatisticType.TTL in type)
        {
            out.pttl = "Limit 6h/dzień"
            out.ttl = player.data.ttl
        }

        // TODO
        if (StatisticType.ALL in type)
        {
            out.clan = 0
            out.clanrank = 0
            out.fgrp = 0
            out.healpower = 0
            out.img = this.icon
            out.mails = 0
            out.mailsAll = 0
            out.mailsLast = ""
            out.mpath = ""
            out.pvp = 0
            out.bag = 0
            out.party = 0
            out.trade = 0
            out.wanted = 0
            out.stamina = 50
            out.staminaTimestamp = 0
            out.staminaRenew = 0
        }

        return out
    }
}

class StatisticType private constructor(val flag: Int = (1 shl counter++))
{
    companion object
    {
        private var counter = 1

        val NONE = StatisticType()
        val WARRIOR = StatisticType()
        val POSITION = StatisticType()
        val CURRENCY = StatisticType()
        val TTL = StatisticType()

        // all
        val ALL = StatisticType(-1)
    }

    fun and(statisticType: StatisticType): StatisticType
    {
        return StatisticType(this.flag or statisticType.flag)
    }

    operator fun contains(what: StatisticType): Boolean
    {
        if (this == ALL)
        {
            return true
        }
        if (what == ALL)
        {
            return false
        }
        return (this.flag and what.flag) != 0
    }

    operator fun plus(other: StatisticType): StatisticType
    {
        return this.and(other)
    }

    override fun equals(other: Any?): Boolean
    {
        return other is StatisticType && other.flag == this.flag
    }

    override fun hashCode(): Int
    {
        return this.flag
    }
}