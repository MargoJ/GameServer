package pl.margoj.server.implementation.map

import pl.margoj.server.api.map.Town
import java.util.Arrays

data class TownImpl(override val id: String, override val name: String, override val width: Int, override val height: Int, override val collisions: Array<BooleanArray>, val image: ByteArray) : Town
{
    @Suppress("LoopToCallChain")
    val margonemCollisionsString: String
        get()
        {
            val collisionsChain = BooleanArray(this.width * this.height)

            for (x in 0..(this.width - 1))
            {
                for (y in 0..(this.height - 1))
                {
                    collisionsChain[x + y * this.width] = this.collisions[x][y]
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

                    for(p in 0..5)
                    {
                        mask = mask or if(collisionsIndex >= collisionsChain.size) 0 else (if(collisionsChain[collisionsIndex++]) (1 shl p) else 0)
                    }

                    out.append((32 + mask).toChar())
                }
            }

            return out.toString()
        }

    override fun toString(): String
    {
        return "TownImpl(id='$id', name='$name', width=$width, height=$height)"
    }
}