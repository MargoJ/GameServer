package pl.margoj.server.implementation.inventory

import pl.margoj.server.api.inventory.Inventory

interface WrappedInventory
{
    val owner: Inventory
}