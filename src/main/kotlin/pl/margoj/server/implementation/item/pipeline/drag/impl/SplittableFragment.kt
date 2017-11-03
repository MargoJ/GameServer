package pl.margoj.server.implementation.item.pipeline.drag.impl

import pl.margoj.mrf.item.ItemProperties
import pl.margoj.server.implementation.item.pipeline.drag.ItemDragPipelineData
import pl.margoj.server.implementation.pipeline.PipelineFragment

class SplittableFragment : PipelineFragment<ItemDragPipelineData>
{
    companion object
    {
        val INSTANCE = SplittableFragment()
    }

    override fun process(fragment: ItemDragPipelineData)
    {
        if (fragment.item.item.id != fragment.target.item.id)
        {
            return
        }
        if (fragment.item[ItemProperties.MAX_AMOUNT] == 0 || fragment.target[ItemProperties.MAX_AMOUNT] == 0) // not splittable
        {
            return
        }

        val currentAmount = fragment.item[ItemProperties.AMOUNT]
        val targetAmount = fragment.target[ItemProperties.AMOUNT]
        val targetMaxAmount = fragment.target[ItemProperties.MAX_AMOUNT]

        val toTransfer = Math.min(currentAmount, targetMaxAmount - targetAmount)
        if (toTransfer <= 0)
        {
            return
        }

        val newCurrentAmount = currentAmount - toTransfer
        val newTargetAmount = targetAmount + toTransfer

        if(newCurrentAmount == 0)
        {
            fragment.item.destroyItem()
        }
        else
        {
            fragment.item.setProperty(ItemProperties.AMOUNT, newCurrentAmount)
        }

        fragment.target.setProperty(ItemProperties.AMOUNT, newTargetAmount)
    }
}