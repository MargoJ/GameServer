package pl.margoj.server.implementation.npc

import com.google.common.collect.ImmutableList
import pl.margoj.mrf.npc.MargoNpc
import pl.margoj.mrf.npc.NpcGender
import pl.margoj.mrf.npc.NpcProfession
import pl.margoj.mrf.npc.NpcRank
import pl.margoj.server.api.Server
import pl.margoj.server.api.battle.DamageSource
import pl.margoj.server.api.map.ImmutableLocation
import pl.margoj.server.api.player.Gender
import pl.margoj.server.api.player.Profession
import pl.margoj.server.implementation.entity.EntityTracker
import pl.margoj.server.implementation.entity.LivingEntityImpl
import pl.margoj.server.implementation.map.TownImpl
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.network.protocol.jsons.NpcObject
import pl.margoj.server.implementation.npc.parser.parsed.NpcParsedScript
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class Npc(val data: MargoNpc?, val script: NpcParsedScript?, override val location: ImmutableLocation, override val server: Server) : LivingEntityImpl()
{
    var id: Int = npcIdCounter.incrementAndGet()

    override var name: String = ""
    override val direction: Int = 0
    override var icon: String = ""
    override var gender: Gender = Gender.UNKNOWN

    override var level: Int
        get() = this.stats.level
        set(value)
        {
            this.stats.level = value
        }

    override val stats = NpcData(this)
    override var hp: Int = 100
    override var deadUntil: Date? = null

    override val trackingRange: Int = 3
    override val neverDelete: Boolean = true

    var group: Int = 0
    var type: NpcType = NpcType.NPC
    var subType: NpcSubtype = NpcSubtype.NORMAL
    var customSpawnTime: Long? = null

    override val withGroup: List<LivingEntityImpl>
        get()
        {
            if (this.group <= 0)
            {
                return Collections.singletonList(this)
            }

            val out = ImmutableList.builder<LivingEntityImpl>()
            out.add(this)

            val town = this.location.town!! as TownImpl

            for (npc in town.npc)
            {
                if (npc.type == NpcType.MONSTER && npc.group == this.group && npc != this && npc.battleUnavailabilityCause == null)
                {
                    out.add(npc)
                }
            }

            return out.build()
        }

    override fun kill(damageSource: DamageSource?)
    {
        super.kill(damageSource)
        this.deadUntil = Date(System.currentTimeMillis() + this.killTime)
    }

    override val killTime: Long
        get()
        {
            return (this.customSpawnTime ?: super.killTime)
        }

    fun loadData()
    {
        val data = this.data

        if (data != null)
        {
            this.icon = data.graphics
            this.name = data.name
            this.level = data.level

            this.type = when (data.type)
            {
                pl.margoj.mrf.npc.NpcType.NPC -> NpcType.NPC
                pl.margoj.mrf.npc.NpcType.MONSTER -> NpcType.MONSTER
            }
            this.subType = when (data.rank)
            {
                NpcRank.NORMAL -> NpcSubtype.NORMAL
                NpcRank.ELITE -> NpcSubtype.ELITE1
                NpcRank.ELITE_II -> NpcSubtype.ELITE2
                NpcRank.ELITE_III -> NpcSubtype.ELITE3
                NpcRank.HERO -> NpcSubtype.HERO
                NpcRank.TITAN -> NpcSubtype.TITAN
                else -> NpcSubtype.NORMAL
            }
            this.gender = when (data.gender)
            {
                NpcGender.MALE -> Gender.MALE
                NpcGender.FEMALE -> Gender.FEMALE
                NpcGender.UNKNOWN -> Gender.UNKNOWN
                else -> Gender.UNKNOWN
            }
            this.stats.profession = when (data.profession)
            {
                NpcProfession.WARRIOR -> Profession.WARRIOR
                NpcProfession.PALADIN -> Profession.PALADIN
                NpcProfession.BLADE_DANCER -> Profession.BLADE_DANCER
                NpcProfession.MAGE -> Profession.MAGE
                NpcProfession.HUNTER -> Profession.HUNTER
                NpcProfession.TRACKER -> Profession.TRACKER
            }

            this.stats.strength = data.strength
            this.stats.agility = data.agility
            this.stats.intellect = data.intellect
            this.stats.attackSpeed = data.attackSpeed / 100.0
            this.stats.maxHp = data.maxHp
            this.stats.damage = data.attack
            this.stats.armor = data.armor
            this.stats.block = data.block
            this.stats.evade = data.evade

            if (data.spawnTime != 0L)
            {
                this.customSpawnTime = data.spawnTime
            }
        }
    }

    override fun announce(tracker: EntityTracker, out: OutgoingPacket)
    {
        val npc = NpcObject()
        npc.id = this.id
        npc.nick = this.name
        npc.questMark = 0 // TODO
        npc.icon = this.icon
        npc.x = this.location.x
        npc.y = this.location.y
        npc.level = this.level
        npc.type = this.type.margoId
        npc.subType = this.subType.margoId
        npc.group = this.group
        out.addNpc(npc)
    }

    override fun dispose(tracker: EntityTracker, out: OutgoingPacket)
    {
        val npc = NpcObject()
        npc.id = this.id
        npc.del = 1

        out.addNpc(npc)
    }

    override fun update(tracker: EntityTracker, out: OutgoingPacket)
    {
    }

    override fun toString(): String
    {
        return "Npc(id=$id, name=$name, location=$location)"
    }

    companion object
    {
        private val npcIdCounter = AtomicInteger()
    }
}