package pl.margoj.server.implementation.commands.defaults

import pl.margoj.server.api.commands.Arguments
import pl.margoj.server.api.commands.CommandException
import pl.margoj.server.api.commands.CommandListener
import pl.margoj.server.api.commands.CommandSender
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.database.DatabaseManager
import pl.margoj.server.implementation.database.DatabaseObjectCache

class CacheCommand : CommandListener
{
    companion object
    {
        var caches = HashMap<String, (DatabaseManager) -> DatabaseObjectCache<*>>()

        init
        {
            caches.put("players", { it.playerInventoryCache })
            caches.put("inventories", { it.playerInventoryCache })
            caches.put("items", { it.itemDataCache })
        }
    }

    override fun commandPerformed(command: String, sender: CommandSender, args: Arguments)
    {
        if (!args.has(0))
        {
            sender.sendMessage(".$command list - lista cache")
            sender.sendMessage(".$command stat [cache] - statystyki cache")
            sender.sendMessage(".$command flush [cache] - zapisz wszystko do bazy danych")
            sender.sendMessage(".$command discard [cache] [id] - odrzuć zmiany w wybranych cache")
            return
        }

        val selected: MutableMap<String, DatabaseObjectCache<*>> = HashMap(caches.size)

        if (!args.has(1))
        {
            selected.putAll(caches.mapValues { it.value((sender.server as ServerImpl).databaseManager) })
        }
        else
        {
            val supplier = caches[args.asString(1)]
            args.ensureNotNull(supplier, "Nie znaleziono podanego cache (${args.asString(1)})")
            selected.put(args.asString(1), supplier!!((sender.server as ServerImpl).databaseManager))
        }

        when (args.asString(0))
        {
            "list" ->
            {
                sender.sendMessage("Lista cache:")
                caches.keys.forEach { sender.sendMessage(" - $it") }
            }
            "stat" ->
            {
                for ((name, cache) in selected)
                {
                    sender.sendMessage("=== Statystyki cache: $name")
                    sender.sendMessage("Ilośc elementów w cache: ${cache.cached}")
                    val lastSave: String
                    if (cache.lastSave == 0L)
                    {
                        lastSave = "nigdy"
                    }
                    else
                    {
                        lastSave = ((System.currentTimeMillis() - cache.lastSave) / 1000L).toString() + " sekund temu"
                    }
                    sender.sendMessage("Ostatni zapis: $lastSave")
                }
            }
            "flush" ->
            {
                for ((name, cache) in selected)
                {
                    sender.server.ticker.runAsync(object : NamedCacheTask(name, cache)
                    {
                        override fun run()
                        {
                            sender.sendMessage("$cacheName flush - rozpoczęto")
                            val before = cache.cached
                            cache.saveToDatabase()
                            val after = cache.cached
                            sender.sendMessage("$cacheName flush - zakończono (zapisano - $before, zwolniono - ${before - after})")
                        }
                    })
                }
            }
            "discard" ->
            {
                args.ensureTrue({ args.has(2) }, "Nie podano nazwy lub id")
                val id = args.asLong(2)
                args.ensureNotNull(id, "${args.asString(0)} nie jest poprawnym id")

                val (name, cache) = selected.iterator().next()
                @Suppress("UNCHECKED_CAST")
                cache as DatabaseObjectCache<Any>
                sender.sendMessage("Usuwam obiekt od id '$id' z cache '$name' ...")

                val toDiscard = cache.getOnlyFromCache(id!!)
                args.ensureNotNull(toDiscard, "Nie znaleziono obiektu'")
                args.ensureTrue({ cache.canWipe(toDiscard!!) }, "Nie można usunąc tego obiektu z cache (obiekt używany)")
                cache.remove(id)
            }
            else ->
            {
                throw CommandException("Nieznana subkomenda, użyj .$command aby zobaczyć help")
            }
        }
    }

    private abstract class NamedCacheTask(val cacheName: String, val cache: DatabaseObjectCache<*>) : Runnable
}