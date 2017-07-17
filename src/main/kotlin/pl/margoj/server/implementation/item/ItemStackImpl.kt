package pl.margoj.server.implementation.item

import pl.margoj.server.api.inventory.ItemStack

class ItemStackImpl(override val item: ItemImpl, override val id: Long) : ItemStack