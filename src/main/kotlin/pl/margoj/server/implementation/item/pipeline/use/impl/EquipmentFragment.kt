package pl.margoj.server.implementation.item.pipeline.use.impl

import pl.margoj.mrf.item.ItemCategory
import pl.margoj.server.api.inventory.ItemStack
import pl.margoj.server.implementation.item.pipeline.use.ItemUsePipelineData
import pl.margoj.server.implementation.item.pipeline.use.ItemUsePipelineFragment

class EquipmentFragment : ItemUsePipelineFragment
{
    override fun process(fragment: ItemUsePipelineData)
    {
        fragment.equipped = true

        val inv = fragment.user.inventory.equipment
        val item = fragment.item

        fragment.returnValue = when (item.item.margoItem.itemCategory)
        {
            ItemCategory.HELMET -> this.equipTo({ inv.helmet }, { inv.helmet = item })
            ItemCategory.RINGS -> this.equipTo({ inv.ring }, { inv.ring = item })
            ItemCategory.NECKLACES -> this.equipTo({ inv.neckless }, { inv.neckless = item })
            ItemCategory.GLOVES -> this.equipTo({ inv.gloves }, { inv.gloves = item })

            ItemCategory.ONE_HANDED_WEAPONS -> this.equipTo({ inv.weapon }, { inv.weapon = item })
            ItemCategory.TWO_HANDED_WEAPONS -> this.equipTo({ inv.weapon }, { inv.weapon = item })
            ItemCategory.HAND_AND_A_HALF_WEAPONS -> this.equipTo({ inv.weapon }, { inv.weapon = item })
            ItemCategory.RANGE_WEAPON -> this.equipTo({ inv.weapon }, { inv.weapon = item })
            ItemCategory.STAFF -> this.equipTo({ inv.weapon }, { inv.weapon = item })
            ItemCategory.WANDS -> this.equipTo({ inv.weapon }, { inv.weapon = item })
            ItemCategory.ARMOR -> this.equipTo({ inv.armor }, { inv.armor = item })

            ItemCategory.HELPERS -> this.equipTo({ inv.helper }, { inv.helper = item })
            ItemCategory.ARROWS -> this.equipTo({ inv.helper }, { inv.helper = item })
            ItemCategory.SHIELDS -> this.equipTo({ inv.helper }, { inv.helper = item })

            ItemCategory.BOOTS -> this.equipTo({ inv.boots }, { inv.boots = item })

            else ->
            {
                fragment.equipped = false
                null
            }
        }
    }

    private inline fun equipTo(get: () -> ItemStack?, set: () -> Unit): ItemStack?
    {
        val previous = get()
        set()
        return previous
    }
}