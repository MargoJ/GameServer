package pl.margoj.server.implementation.map

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import pl.margoj.mrf.map.metadata.pvp.MapPvP
import pl.margoj.mrf.map.metadata.welcome.WelcomeMessage
import pl.margoj.mrf.map.objects.gateway.GatewayObject
import pl.margoj.mrf.map.objects.mapspawn.MapSpawnObject
import pl.margoj.server.api.map.ImmutableLocation
import pl.margoj.server.implementation.network.protocol.jsons.TownObject
import pl.margoj.server.implementation.utils.GsonUtils

class CachedMapData(val town: TownImpl)
{
    lateinit var townObject: TownObject
        private set

    lateinit var townElement: JsonElement
        private set

    lateinit var gw2: JsonArray
        private set

    lateinit var townname: JsonObject
        private set

    lateinit var collisionString: String
        private set

    lateinit var spawnPoint: ImmutableLocation
        private set

    fun update()
    {
        this.townObject = TownObject(
                mapId = town.numericId,
                mainMapId = if (town.parentMap == null) 0 else town.parentMap!!.numericId,
                width = town.width,
                height = town.height,
                imageFileName = "${town.id}.png",
                mapName = town.name,
                pvp = town.getMetadata(MapPvP::class.java).margonemId,
                water = this.createWaterString(),
                battleBackground = "aa1.jpg",
                welcomeMessage = town.getMetadata(WelcomeMessage::class.java).value
        )

        this.townElement = GsonUtils.gson.toJsonTree(this.townObject)

        this.gw2 = JsonArray()
        this.townname = JsonObject()

        for (mapObject in town.objects)
        {
            val gateway = mapObject as? GatewayObject ?: continue
            val targetMap = town.server.getTownById(gateway.targetMap) ?: continue

            this.gw2.add(targetMap.numericId)
            this.gw2.add(gateway.position.x)
            this.gw2.add(gateway.position.y)
            this.gw2.add(if (gateway.keyId == null) 0 else 1) /// needs key

            // level restrictions
            if (gateway.levelRestriction.enabled)
            {
                val minLevel = gateway.levelRestriction.minLevel
                val maxLevel = gateway.levelRestriction.maxLevel
                this.gw2.add(minLevel or (maxLevel shl 16))
            }
            else
            {
                this.gw2.add(0)
            }

            this.townname.add(targetMap.numericId.toString(), JsonPrimitive(targetMap.name))
        }

        this.collisionString = this.createCollisionString()

        this.spawnPoint = this.findSpawnPoint() ?: throw IllegalStateException("no spawn point in map ${this.town.id}")
    }

    private fun createCollisionString(): String
    {
        val collisionsChain = BooleanArray(this.town.width * this.town.height)

        for (x in 0 until this.town.width)
        {
            for (y in 0 until this.town.height)
            {
                collisionsChain[x + y * this.town.width] = this.town.collisions[x][y]
            }
        }

        val out = StringBuilder()

        var collisionsIndex = 0

        while (collisionsIndex < collisionsChain.size)
        {
            var zerosMultiplier = 0

            zeros_loop@
            while (true)
            {
                for (zerosShift in 0..5)
                {
                    if (collisionsIndex + zerosShift >= collisionsChain.size || collisionsChain[collisionsIndex + zerosShift])
                    {
                        break@zeros_loop
                    }
                }
                collisionsIndex += 6
                zerosMultiplier++
            }

            if (zerosMultiplier > 0)
            {
                while (zerosMultiplier > 27)
                {
                    out.append('z')
                    zerosMultiplier -= 27
                }

                if (zerosMultiplier > 0)
                {
                    out.append(('_'.toInt() + zerosMultiplier).toChar())
                }
            }
            else
            {
                var mask = 0

                for (p in 0..5)
                {
                    mask = mask or if (collisionsIndex >= collisionsChain.size) 0 else (if (collisionsChain[collisionsIndex++]) (1 shl p) else 0)
                }

                out.append((32 + mask).toChar())
            }
        }

        return out.toString()
    }


    private fun createWaterString(): String
    {
        val waterString = StringBuilder()

        for (x in 0 until this.town.width)
        {
            for (y in 0 until this.town.height)
            {
                val current = this.town.water[x][y]
                if (current != 0)
                {
                    waterString.append(x).append(",").append(x).append(",").append(y).append(",").append(current).append("|")
                }
            }
        }

        if (waterString.isNotEmpty())
        {
            waterString.setLength(waterString.length - 1)
        }

        return waterString.toString()
    }

    private fun findSpawnPoint(): ImmutableLocation?
    {
        var targetX: Int? = null
        var targetY: Int? = null

        val spawnPoint = town.objects.find { it is MapSpawnObject }
        if (spawnPoint != null)
        {
            targetX = spawnPoint.position.x
            targetY = spawnPoint.position.y
        }
        else
        {
            var x = 0
            var y = 0

            loop@
            while (x < town.width)
            {
                while (y < town.height)
                {
                    if (!town.collisions[x][y])
                    {
                        targetX = x
                        targetY = y
                        break@loop
                    }
                    y++
                }
                x++
            }
        }

        if(targetX == null || targetY == null)
        {
            return null
        }
        return ImmutableLocation(this.town, targetX, targetY)
    }
}