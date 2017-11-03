package pl.margoj.server.implementation.item.pipeline.use.impl

import pl.margoj.mrf.item.ItemProperties
import pl.margoj.server.implementation.item.pipeline.use.ItemUsePipelineData
import pl.margoj.server.implementation.item.pipeline.use.ItemUsePipelineFragment

class BindingFragment : ItemUsePipelineFragment
{
    override fun process(fragment: ItemUsePipelineData)
    {
        if (fragment.equipped && fragment.item[ItemProperties.BINDS])
        {
            fragment.item.setProperty(ItemProperties.BINDS, false)
            fragment.item.setProperty(ItemProperties.SOUL_BOUND, true)
        }
    }
}