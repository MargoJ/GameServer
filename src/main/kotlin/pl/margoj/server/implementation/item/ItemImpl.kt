package pl.margoj.server.implementation.item

import pl.margoj.mrf.item.MargoItem
import pl.margoj.server.api.inventory.Item

class ItemImpl(val margoItem: MargoItem) : Item
{
    override val id: String get() = this.margoItem.id
    override val name: String get() = this.margoItem.name

    override fun hashCode(): Int
    {
        return this.id.hashCode()
    }

    override fun equals(other: Any?): Boolean
    {
        return other is ItemImpl && other.id == this.id
    }

    override fun toString(): String
    {
        return "ItemImpl(margoItem=$margoItem)"
    }

    fun toSimpleString(): String
    {
        return "$id[$name]"
    }
}