package pl.margoj.server.implementation.item.pipeline.use

import pl.margoj.server.api.inventory.ItemStack
import pl.margoj.server.implementation.item.ItemStackImpl
import pl.margoj.server.implementation.item.pipeline.ItemPipelineData
import pl.margoj.server.implementation.pipeline.CancelablePipelineElement
import pl.margoj.server.implementation.player.PlayerImpl

open class ItemUsePipelineData(item: ItemStackImpl, user: PlayerImpl) : ItemPipelineData(item, user), CancelablePipelineElement
{
    override var canceled: Boolean = false

    var returnValue: ItemStack? = null

    /** was an item equipped */
    var equipped = false

    /** should this item be put on cooldown if possible? */
    var putOnCooldown = false

    /** should this item be taken after use */
    var take = false
}