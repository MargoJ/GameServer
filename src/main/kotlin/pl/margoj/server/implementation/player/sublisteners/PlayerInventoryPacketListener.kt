package pl.margoj.server.implementation.player.sublisteners

import pl.margoj.server.api.utils.Parse
import pl.margoj.server.implementation.network.protocol.IncomingPacket
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.player.PlayerConnection

class PlayerInventoryPacketListener(connection: PlayerConnection) : PlayerPacketSubListener(connection, onlyOnPlayer = true)
{
    override fun handle(packet: IncomingPacket, out: OutgoingPacket, query: Map<String, String>): Boolean
    {
        if (packet.type == "moveitem" && query["st"] == "0")
        {
            val inventory = player!!.inventory

            val item = inventory.getItemstackById(Parse.parseLong(query["id"])!!)
            val x = Parse.parseInt(query["x"])!!
            val (y, bagId) = inventory.margoYToRealYAndBagId(Parse.parseInt(query["y"])!!)

            if (item != null)
            {
                inventory.getBagInventory(bagId).setItemAt(x, y, item)
            }
        }

        return true
    }
}