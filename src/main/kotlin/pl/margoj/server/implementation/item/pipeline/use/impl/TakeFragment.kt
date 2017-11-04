package pl.margoj.server.implementation.item.pipeline.use.impl

import pl.margoj.mrf.item.ItemProperties
import pl.margoj.server.implementation.item.pipeline.use.ItemUsePipelineData
import pl.margoj.server.implementation.item.pipeline.use.ItemUsePipelineFragment

class TakeFragment : ItemUsePipelineFragment
{
    override fun process(fragment: ItemUsePipelineData)
    {
        if (!fragment.take || fragment.item[ItemProperties.AMOUNT] == 0)
        {
            return
        }

        var amount = fragment.item[ItemProperties.AMOUNT]
        amount--

        if(amount > 0)
        {
            fragment.item.setProperty(ItemProperties.AMOUNT, amount)
        }
        else
        {
            fragment.item.destroyItem()
        }
    }
}