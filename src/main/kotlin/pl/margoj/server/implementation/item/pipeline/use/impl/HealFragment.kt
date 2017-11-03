package pl.margoj.server.implementation.item.pipeline.use.impl

import pl.margoj.mrf.item.ItemCategory
import pl.margoj.mrf.item.ItemProperties
import pl.margoj.server.implementation.item.pipeline.use.ItemUsePipelineData
import pl.margoj.server.implementation.item.pipeline.use.ItemUsePipelineFragment

class HealFragment : ItemUsePipelineFragment
{
    override fun process(fragment: ItemUsePipelineData)
    {
        val item = fragment.item
        val player = fragment.user

        if (item[ItemProperties.CATEGORY] == ItemCategory.CONSUMABLE)
        {
            val heal = item[ItemProperties.HEAL]
            if (heal != 0)
            {
                player.damage(-heal)
                fragment.putOnCooldown = true
                fragment.take = true
            }

            val healPercent = item[ItemProperties.HEAL_PERCENT]
            if (healPercent != 0)
            {
                val healAmount = (player.stats.maxHp * (healPercent.toDouble() / 100.0)).toInt()
                player.damage(-healAmount)
                fragment.putOnCooldown = true
                fragment.take = true
            }

            val fullHeal = item[ItemProperties.FULL_HEAL]
            if (fullHeal != 0)
            {
                val healAmount = Math.min((player.stats.maxHp - player.hp), fullHeal)

                if(healAmount != 0)
                {
                    fragment.putOnCooldown = true
                    player.damage(-healAmount)

                    val left = fullHeal - healAmount
                    fragment.item.setProperty(ItemProperties.FULL_HEAL, left)

                    if(left <= 0)
                    {
                        fragment.take = true
                    }
                }
            }
        }
    }
}