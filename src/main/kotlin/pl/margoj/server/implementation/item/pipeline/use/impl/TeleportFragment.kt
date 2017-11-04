package pl.margoj.server.implementation.item.pipeline.use.impl

import pl.margoj.mrf.item.ItemCategory
import pl.margoj.mrf.item.ItemProperties
import pl.margoj.server.api.map.Location
import pl.margoj.server.implementation.item.pipeline.use.ItemUsePipelineData
import pl.margoj.server.implementation.item.pipeline.use.ItemUsePipelineFragment

class TeleportFragment : ItemUsePipelineFragment
{
    override fun process(fragment: ItemUsePipelineData)
    {
        val item = fragment.item
        val player = fragment.user

        if (item[ItemProperties.CATEGORY] == ItemCategory.CONSUMABLE)
        {
            val teleport = item[ItemProperties.TELEPORT]

            if (teleport.map.isNotEmpty())
            {
                val town = player.server.getTownById(teleport.map)!!

                if(teleport.customCoords)
                {
                    val location = Location(town, teleport.x, teleport.y)
                    player.teleport(location)
                }
                else
                {
                    player.teleport(town)
                }

                fragment.take = true
                fragment.putOnCooldown = true
            }
        }
    }
}