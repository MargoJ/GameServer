package pl.margoj.server.implementation.item.pipeline.drag

import pl.margoj.server.implementation.item.ItemStackImpl
import pl.margoj.server.implementation.item.pipeline.ItemPipelineData
import pl.margoj.server.implementation.pipeline.CancelablePipelineElement
import pl.margoj.server.implementation.player.PlayerImpl

open class ItemDragPipelineData(item: ItemStackImpl, val target: ItemStackImpl, user: PlayerImpl) : ItemPipelineData(item, user), CancelablePipelineElement
{
    override var canceled: Boolean = false


}