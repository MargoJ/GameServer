package pl.margoj.server.implementation.item

import pl.margoj.mrf.item.MargoItem
import pl.margoj.server.api.inventory.Item
import java.io.File

class ItemImpl(val margoItem: MargoItem, val imgFileLocation: File?, val imgFileName: String) : Item
{
    override val id: String get() = this.margoItem.id

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
        return "ItemImpl(margoItem=$margoItem, imgFileLocation=$imgFileLocation, imgFileName='$imgFileName')"
    }
}