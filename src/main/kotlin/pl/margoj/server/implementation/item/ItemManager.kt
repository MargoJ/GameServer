package pl.margoj.server.implementation.item

import pl.margoj.mrf.item.ItemProperty
import pl.margoj.server.api.inventory.ItemStack
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.network.protocol.jsons.ItemObject

class ItemManager(val server: ServerImpl)
{
    private var TODO_itemCounter: Long = 0 // TODO

    fun getNextItemId(): Long
    {
        return TODO_itemCounter++
    }

    fun newItemStack(item: ItemImpl): ItemStackImpl
    {
        return ItemStackImpl(item, this.getNextItemId())
    }

    @Suppress("UNCHECKED_CAST")
    fun createItemObject(itemStack: ItemStackImpl): ItemObject
    {
        val itemObject = ItemObject(id = itemStack.id, hid = itemStack.id, statistics = "")

        val item = itemStack.item

        for (property in ItemProperty.properties)
        {
            var parser: ItemPropertyParser<Any?, ItemProperty<Any?>>? = null

            for (propertyParser in ItemPropertyParser.ALL)
            {
                if (propertyParser.propertyType.isInstance(property))
                {
                    parser = propertyParser as ItemPropertyParser<Any?, ItemProperty<Any?>>
                }
            }

            if (parser == null)
            {
                server.logger.warn("Nie można sparsować właściwośći ${property.javaClass.name}, brak parsera!")
                continue
            }

            parser.apply(property as ItemProperty<Any?>, itemObject, item.margoItem[property], itemStack, item)
        }

        return itemObject
    }
}