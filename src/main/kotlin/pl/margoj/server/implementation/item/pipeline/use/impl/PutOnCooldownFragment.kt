package pl.margoj.server.implementation.item.pipeline.use.impl

import pl.margoj.mrf.item.ItemProperties
import pl.margoj.mrf.item.properties.special.CooldownProperty
import pl.margoj.server.implementation.item.pipeline.use.ItemUsePipelineData
import pl.margoj.server.implementation.item.pipeline.use.ItemUsePipelineFragment
import java.util.concurrent.TimeUnit

class PutOnCooldownFragment : ItemUsePipelineFragment
{
    override fun process(fragment: ItemUsePipelineData)
    {
        if(!fragment.putOnCooldown)
        {
            return
        }

        val cooldownProperty = fragment.item[ItemProperties.COOLDOWN]
        if(cooldownProperty.cooldown == 0)
        {
            return
        }

        val newCooldown = CooldownProperty.Cooldown(cooldownProperty.cooldown, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(cooldownProperty.cooldown.toLong()))
        fragment.item.setProperty(ItemProperties.COOLDOWN, newCooldown)
    }
}