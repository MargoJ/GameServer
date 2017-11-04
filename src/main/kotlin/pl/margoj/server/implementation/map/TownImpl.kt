package pl.margoj.server.implementation.map

import pl.margoj.mrf.map.Point
import pl.margoj.mrf.map.metadata.MetadataElement
import pl.margoj.mrf.map.metadata.ismain.IsMain
import pl.margoj.mrf.map.metadata.istown.IsTown
import pl.margoj.mrf.map.metadata.parentmap.ParentMap
import pl.margoj.mrf.map.metadata.pvp.MapPvP
import pl.margoj.mrf.map.metadata.respawnmap.RespawnMap
import pl.margoj.mrf.map.objects.MapObject
import pl.margoj.mrf.map.objects.npc.NpcMapObject
import pl.margoj.mrf.map.objects.text.TextMapObject
import pl.margoj.mrf.map.serialization.MapData
import pl.margoj.server.api.chat.ChatMessage
import pl.margoj.server.api.map.ImmutableLocation
import pl.margoj.server.api.map.PvPStatus
import pl.margoj.server.api.map.Town
import pl.margoj.server.api.utils.toTime
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.inventory.map.MapInventoryImpl
import pl.margoj.server.implementation.npc.Npc
import pl.margoj.server.implementation.npc.NpcType
import java.io.File
import java.util.LinkedList

data class TownImpl(
        val server: ServerImpl,
        override val numericId: Int,
        override val id: String,
        override val name: String,
        override val width: Int,
        override val height: Int,
        override val collisions: Array<BooleanArray>,
        override val water: Array<IntArray>,
        val metadata: Collection<MetadataElement>,
        val objects: Collection<MapObject<*>>,
        val image: File,
        val partList: Map<Point, Int>
) : Town
{
    val cachedMapData: CachedMapData = CachedMapData(this)

    private var updatedOnce = false

    override var parentMap: Town? = null
        private set

    override lateinit var respawnMap: Town
        private set

    override lateinit var inventory: MapInventoryImpl
    private var npcs_ = ArrayList<Npc>()
    val npc: Collection<Npc> get() = this.npcs_

    override val pvp: PvPStatus
        get() = when (this.getMetadata(MapPvP::class.java))
        {
            MapPvP.NO_PVP -> PvPStatus.NO_PVP
            MapPvP.CONDITIONAL -> PvPStatus.CONDITIONAL
            MapPvP.ARENAS -> PvPStatus.ARENAS
            MapPvP.UNCONDITIONAL -> PvPStatus.UNCONDITIONAL
            else -> PvPStatus.CONDITIONAL
        }

    override val isTown: Boolean
        get() = this.getMetadata(IsTown::class.java).value

    override val isMain: Boolean
        get() = this.getMetadata(IsMain::class.java).value

    override val spawnPoint: ImmutableLocation
        get() = this.cachedMapData.spawnPoint

    val chatHistory = LinkedList<ChatMessage>()

    @Suppress("UNCHECKED_CAST")
    fun <T : MetadataElement> getMetadata(clazz: Class<T>): T
    {
        val optional = this.metadata.stream().filter { clazz.isInstance(it) }.findAny()

        if (optional.isPresent)
        {
            return optional.get() as T
        }

        return MapData.mapMetadata.values.stream().filter { clazz.isInstance(it.defaultValue) }.findAny().map { it.defaultValue }.get() as T
    }

    fun updateNpcs()
    {
        for (npc in this.npcs_)
        {
            this.server.entityManager.unregisterEntity(npc)
        }

        this.npcs_.clear()

        for (mapObject in this.objects)
        {
            if (mapObject is NpcMapObject)
            {
                val script = if (mapObject.script == null) null else this.server.npcScriptParser.getNpcScript(mapObject.script!!)
                val npc = Npc(script, ImmutableLocation(this, mapObject.position.x, mapObject.position.y), this.server)
                npc.loadData()

                npc.takeIf { mapObject.graphics != null }?.icon = mapObject.graphics!!
                npc.takeIf { mapObject.name != null }?.name = mapObject.name!!
                npc.takeIf { mapObject.level != null }?.level = mapObject.level!!
                npc.takeIf { mapObject.group != null }?.group = mapObject.group!!
                npc.takeIf { mapObject.spawnTime != null }?.customSpawnTime = mapObject.spawnTime!!.toTime()

                this.server.entityManager.registerEntity(npc)
                this.npcs_.add(npc)
            }
            else if (mapObject is TextMapObject)
            {
                val npc = Npc(null, ImmutableLocation(this, mapObject.position.x, mapObject.position.y), this.server)
                npc.type = NpcType.INTERACTIVE
                npc.name = mapObject.text
                npc.icon = "reserved/transparent.png"

                this.server.entityManager.registerEntity(npc)
                this.npcs_.add(npc)
            }
        }

        for ((point, id) in this.partList)
        {
            val npc = Npc(null, ImmutableLocation(this, point.x, point.y), this.server)
            npc.type = NpcType.TRANSPARENT
            npc.icon = "parts/${this.id}_$id.png"
            npc.level = 0
            npc.name = "P $id"
            this.server.entityManager.registerEntity(npc)
            this.npcs_.add(npc)
        }
    }

    fun updateParentMaps()
    {
        this.updatedOnce = true

        if (this.isMain)
        {
            this.parentMap = null
        }
        else
        {
            this.parentMap = this.server.getTownById(this.getMetadata(ParentMap::class.java).value)
        }

        var possibleRespawnMap = this.server.getTownById(this.getMetadata(RespawnMap::class.java).value)

        if (possibleRespawnMap == null && this.parentMap != null)
        {
            val parent = this.parentMap!! as TownImpl

            if (!parent.updatedOnce)
            {
                parent.updateParentMaps()
            }

            possibleRespawnMap = parent.respawnMap as TownImpl
        }

        if (possibleRespawnMap == null)
        {
            // TODO: DEFAULT MAP
            throw IllegalArgumentException("couldn't find respawn map for '${this.id}', respawn id: '${this.getMetadata(RespawnMap::class.java).value}'")
        }
        else
        {
            this.respawnMap = possibleRespawnMap
        }
    }

    fun inBounds(point: Point): Boolean
    {
        return point.x >= 0 && point.y >= 0 && point.x < this.width && point.y < this.height
    }

    fun getObject(point: Point): MapObject<*>?
    {
        return this.objects.stream().filter { it.position == point }.findAny().orElse(null)
    }

    override fun toString(): String
    {
        return "TownImpl(id='$id', name='$name', width=$width, height=$height)"
    }
}