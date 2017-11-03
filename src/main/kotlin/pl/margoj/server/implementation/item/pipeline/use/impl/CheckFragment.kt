package pl.margoj.server.implementation.item.pipeline.use.impl

import pl.margoj.server.implementation.item.pipeline.use.ItemUsePipelineData
import pl.margoj.server.implementation.item.pipeline.use.ItemUsePipelineFragment

class CheckFragment : ItemUsePipelineFragment
{
    override fun process(fragment: ItemUsePipelineData)
    {
        val player = fragment.user

        if(player.inActiveBattle || player.isDead)
        {
            fragment.canceled = false
        }
    }
}