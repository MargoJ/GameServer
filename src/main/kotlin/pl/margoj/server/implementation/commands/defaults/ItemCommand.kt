package pl.margoj.server.implementation.commands.defaults

import org.apache.commons.lang3.StringUtils
import pl.margoj.server.api.Server
import pl.margoj.server.api.commands.Arguments
import pl.margoj.server.api.commands.CommandListener
import pl.margoj.server.api.commands.CommandSender
import pl.margoj.server.api.inventory.Item
import pl.margoj.server.api.player.Player
import pl.margoj.server.api.utils.Paged
import java.util.stream.Collectors

class ItemCommand : CommandListener
{
    override fun commandPerformed(command: String, sender: CommandSender, args: Arguments)
    {
        if (!args.has(0))
        {
            this.showHelp(sender)
            return
        }

        when (args.asString(0))
        {
            "list", "find" ->
            {
                val pageArg = if (args.asString(0) == "list") 1 else 2
                val page = (if (args.has(pageArg)) args.asInt(pageArg) else null) ?: 1

                val pagedItems = if (args.asString(0) == "list")
                {
                    PagedItems(sender.server)
                }
                else
                {
                    PagedItems(sender.server, args.asString(1))
                }

                args.ensureTrue({ pagedItems.size != 0 }, "Nie znaleziono podanego przedmiotu")
                args.ensureTrue({ pagedItems.isThereAPage(page) }, "Nie znaleziono strony $page")

                sender.sendMessage("Lista przedmiotów ($page/${pagedItems.maxPage}): ")
                for (currentItem in pagedItems.getValuesForPage(page))
                {
                    sender.sendMessage(".${currentItem.id} [${currentItem.name}]")
                }

                if (pagedItems.isThereANextPage(page))
                {
                    if (args.asString(0) == "list")
                    {
                        sender.sendMessage("Aby zobaczyć kolejną strone użyj: .item list ${page + 1}")
                    }
                    else
                    {
                        sender.sendMessage("Aby zobaczyć kolejną strone użyj: .item find ${args.asString(1)} ${page + 1}")
                    }
                }
            }
            "give" ->
            {
                args.ensureTrue({ args.has(1) }, "Podaj ID przedmiotu (.item give [id] [opcjonalnie: nazwa gracza])")
                val item = sender.server.getItemById(args.asString(1))

                args.ensureNotNull(item, "Nie znaleziono przedmiotu ${args.asString(1)}")
                item!!

                val target = if (args.has(2))
                {
                    val player = args.asPlayer(2)
                    args.ensureNotNull(player, "Gracz ${args.asString(2)} nie został znaleziony")
                    player!!
                }
                else
                {
                    args.ensureTrue({ sender is Player }, "Aby użyć tej komendy z konsoli podaj gracza (.item give [id] [nazwa gracza]0")
                    sender as Player
                }

                val success = target.inventory.tryToPut(target.server.newItemStack(item))
                args.ensureTrue({ success }, "Nie udało się dodać przedmiotu (pełny ekwipunek)")

                sender.sendMessage("Dodano przedmiot ${item.id} [${item.name}] do ekwipunku gracza ${target.name}")
            }
            "clearinv" ->
            {
                val target = if (args.has(1))
                {
                    val player = args.asPlayer(1)
                    args.ensureNotNull(player, "Gracz ${args.asString(1)} nie został znaleziony")
                    player!!
                }
                else
                {
                    args.ensureTrue({ sender is Player }, "Aby użyć tej komendy z konsoli podaj gracza (.item clear)")
                    sender as Player
                }

                sender.addConfirmationTask({
                    val inventory = target.inventory
                    for (i in 0 until inventory.equipment.size)
                    {
                        inventory.equipment[i] = null
                    }

                    for (bagInventory in inventory.bagInventories)
                    {
                        for (i in 0 until bagInventory.size)
                        {
                            bagInventory[i] = null
                        }
                    }

                    for (bag in 0..3)
                    {
                        inventory.setBag(bag, null)
                    }

                    // TODO: DEFAULT BAG

                    sender.sendMessage("Wymazano cały ekwipunek gracza ${target.name}!")
                }, "Czy na pewno chcesz WYMAZAĆ CAŁY EKWIPUNEK gracza ${target.name}? Wpisz .confirm aby potwierdzić")
            }
            else -> this.showHelp(sender)
        }
    }

    private fun showHelp(sender: CommandSender)
    {
        sender.sendMessage("Lista dostępnych subkomend")
        sender.sendMessage(".item list - Lista wszystkich dostępnych przedmiotów")
        sender.sendMessage(".item find - Szuka podanego przedmiotu")
        sender.sendMessage(".item give - Dodaje przedmiot do ekwipunku")
        sender.sendMessage(".item clearinv - Czysci caly ekwipunek gracza")
    }

    private class PagedItems(val server: Server, val find: String? = null) : Paged<Item>()
    {
        private val sortedItems by lazy {
            if (find == null)
            {
                this.server.items.sortedBy { it.id }
            }
            else
            {
                this.server.items
                        .stream()
                        .filter { StringUtils.containsIgnoreCase(it.id, find) || StringUtils.containsIgnoreCase(it.name, find) }
                        .sorted { o1, o2 -> o1.id.compareTo(o2.id) }
                        .collect(Collectors.toList())
            }
        }

        override val size: Int get () = this.sortedItems.size
        override val entriesPerPage: Int = 10

        override fun getAt(id: Int): Item
        {
            return sortedItems[id]
        }
    }
}