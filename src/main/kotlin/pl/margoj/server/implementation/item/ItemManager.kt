package pl.margoj.server.implementation.item

import pl.margoj.mrf.item.ItemProperties
import pl.margoj.mrf.item.ItemProperty
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.network.protocol.jsons.ItemObject
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class ItemManager(val server: ServerImpl)
{
    private var TODO_itemCounter: AtomicLong = AtomicLong(2_000_000L) // TODO
    private var creating = AtomicBoolean()

    fun getNextItemId(): Long
    {
        return TODO_itemCounter.get()
    }

    fun increaseItemId()
    {
        TODO_itemCounter.incrementAndGet()
    }

    @Synchronized
    fun newItemStack(item: ItemImpl): ItemStackImpl
    {
        this.creating.set(true)
        val itemStack = ItemStackImpl(this, item, this.getNextItemId())
        this.creating.set(false)

        this.increaseItemId()
        return itemStack
    }

    fun validate(id: Long)
    {
        if (!this.creating.get() || id != this.TODO_itemCounter.get())
        {
            throw IllegalStateException("Illegal itemstack creation")
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun createItemObject(itemStack: ItemStackImpl): ItemObject
    {
        val itemObject = ItemObject(id = itemStack.id, hid = itemStack.id, statistics = "")

        val item = itemStack.item

        val restricted = item.margoItem[ItemProperties.NO_DESCRIPTION]

        for (property in ItemProperty.properties)
        {
            if(restricted && !property.showWhenRestricted)
            {
                continue
            }

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

            parser.apply(property as ItemProperty<Any?>, itemObject, itemStack.additionalProperties[property] ?: item.margoItem[property], itemStack, item)
        }

        return itemObject
    }
}