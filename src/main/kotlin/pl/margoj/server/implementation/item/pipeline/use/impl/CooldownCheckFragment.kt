package pl.margoj.server.implementation.item.pipeline.use.impl

import pl.margoj.mrf.item.ItemProperties
import pl.margoj.server.api.inventory.player.ItemIsOnCooldownException
import pl.margoj.server.implementation.item.pipeline.use.ItemUsePipelineData
import pl.margoj.server.implementation.item.pipeline.use.ItemUsePipelineFragment

class CooldownCheckFragment : ItemUsePipelineFragment
{
    override fun process(fragment: ItemUsePipelineData)
    {
        val cooldownProperty = fragment.item[ItemProperties.COOLDOWN]

        if (cooldownProperty.cooldown == 0 || cooldownProperty.nextUse == 0L)
        {
            return
        }

        if (cooldownProperty.nextUse > System.currentTimeMillis())
        {
            throw ItemIsOnCooldownException(cooldownProperty.nextUse - System.currentTimeMillis())
        }
    }
}