package pl.margoj.server.implementation.npc.parser.buildin

import pl.margoj.server.api.battle.BattleUnableToStartException
import pl.margoj.server.api.map.Location
import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext
import pl.margoj.server.implementation.player.PlayerImpl

class PlayerBuildInVariable(val player: PlayerImpl) : BuildInVariable()
{
    override fun getValue(context: ScriptContext, variableName: String): Any
    {
        return when (variableName)
        {
            "nick" -> player.name
            "level", "poziom" -> player.data.level.toLong()
            "hp", "życie" -> player.hp.toLong()
            "hpprocent" -> player.healthPercent.toLong()
            "maxhp" -> player.data.maxHp.toLong()
            "gold", "złoto" -> player.currencyManager.gold
            "goldlimit", "maxgold", "limitzłota" -> player.currencyManager.goldLimit
            else -> "???"
        }
    }

    override fun execute(context: ScriptContext, functionName: String, parameters: Array<Any>): Any
    {
        when (functionName)
        {
            "posiada", "nie posiada" ->
            {
                val item = this.player.server.getItemById(parameters[0] as String) ?: return false
                return if (functionName == "posiada") this.player.inventory.contains(item) else !this.player.inventory.contains(item)
            }
            "dodaj złoto", "zabierz złoto" ->
            {
                val change = if (functionName == "dodaj złoto") parameters[0] as Long else -(parameters[0] as Long)
                if (!this.player.currencyManager.canFit(change))
                {
                    return false
                }

                player.server.gameLogger.info("${player.name}, npc: ${context.npc?.id}: zmiana złota: $change")

                this.player.currencyManager.giveGold(change)
                return true
            }
            "dodaj", "zabierz" ->
            {
                val item = this.player.server.getItemById(parameters[0] as String) ?: return false

                if (functionName == "dodaj")
                {
                    val itemstack = this.player.server.newItemStack(item)
                    val result = this.player.inventory.tryToPut(itemstack)
                    if (result)
                    {
                        this.player.displayScreenMessage("Otrzymano nowy przedmiot: ${item.name}")
                        player.server.gameLogger.info("${player.name}, npc: ${context.npc?.id}: otrzymano ${item.id}[${item.name}], itemid=${itemstack.id}")
                    }
                    return result
                }
                else
                {
                    for (i in this.player.inventory.allItems)
                    {
                        if (i?.item == item)
                        {
                            this.player.inventory[i.ownerIndex!!] = null
                            this.player.displayScreenMessage("Stracono przedmiot: ${i.item.name}")
                            player.server.gameLogger.info("${player.name}, npc: ${context.npc?.id}: stracono ${item.id}[${item.name}], itemid=${i.id}")
                            return true
                        }
                    }

                    return false
                }
            }
            "ustaw hp" ->
            {
                player.hp = Math.max(0, Math.min((parameters[0] as Long).toInt(), player.data.maxHp))
                return true
            }
            "rozpocznij walkę" ->
            {
                try
                {
                    this.player.server.startBattle(this.player.withGroup, context.npc!!.withGroup)
                    return true
                }
                catch (e: BattleUnableToStartException)
                {
                    return false
                }
            }
            "teleportuj na mape" ->
            {
                val map = this.player.server.getTownById(parameters[0] as String) ?: return false
                this.player.teleport(map)
                return true
            }
            "teleportuj na koordynaty" ->
            {
                val map = this.player.location.town!!
                val x = (parameters[0] as Long).toInt()
                val y = (parameters[1] as Long).toInt()

                if (x !in 0 until map.width || y !in 0 until map.height)
                {
                    return false
                }

                this.player.teleport(Location(map, x, y))
                return true
            }
            "teleportuj do" ->
            {
                val map = this.player.server.getTownById(parameters[0] as String) ?: return false
                val x = (parameters[1] as Long).toInt()
                val y = (parameters[2] as Long).toInt()

                if (x !in 0 until map.width || y !in 0 until map.height)
                {
                    return false
                }

                this.player.teleport(Location(map, x, y))
                return true
            }
            "zabij" ->
            {
                this.player.kill(context.npc)
                return true
            }
            else -> throw IllegalStateException("'$functionName' not found")
        }
    }
}