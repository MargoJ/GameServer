package pl.margoj.server.implementation.item.pipeline

import pl.margoj.server.implementation.item.ItemStackImpl
import pl.margoj.server.implementation.player.PlayerImpl

open class ItemPipelineData(val item: ItemStackImpl, val user: PlayerImpl)