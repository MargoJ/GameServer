package pl.margoj.server.implementation.player.sublisteners

import pl.margoj.server.api.events.player.PlayerJoinEvent
import pl.margoj.utils.commons.numbers.Parse
import pl.margoj.utils.commons.time.TimeUtils
import pl.margoj.server.implementation.database.TableNames
import pl.margoj.server.implementation.map.TownImpl
import pl.margoj.server.implementation.network.protocol.IncomingPacket
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.player.PlayerConnection
import pl.margoj.server.implementation.player.PlayerDataImpl
import pl.margoj.server.implementation.player.PlayerImpl
import pl.margoj.server.implementation.player.StatisticType
import pl.margoj.server.implementation.utils.GsonUtils

class PlayerInitListener(connection: PlayerConnection) : PlayerPacketSubListener(connection, onlyWithType = "init", async = true)
{
    override fun handle(packet: IncomingPacket, out: OutgoingPacket, query: Map<String, String>): Boolean
    {
        val initlvl = Parse.parseInt(query["initlvl"])
        this.checkForMaliciousData(initlvl == null || initlvl !in 1..4, "invalid initlvl: ${query["initlvl"]}")

        connection.manager.server.logger.trace("handleInit, initlvl=$initlvl, aid=${connection.aid}")

        if (initlvl != 1 && player == null)
        {
            out.addAlert("Nie jesteś zalogowany!")
            out.addEngineAction(OutgoingPacket.EngineAction.STOP)
            return false
        }

        when (initlvl)
        {
            1 ->
            {
                if (this.player == null)
                {
                    var data = server.databaseManager.playerDataCache.loadOne(connection.aid)

                    if (data == null)
                    {
                        this.server.gameLogger.info("Rejestruje nową postać: ${this.connection.authSession.charName}")

                        server.databaseManager.withConnection {
                            val statement = it.prepareStatement("INSERT INTO `${TableNames.PLAYERS}`(`id`, `accountId`, `characterName`, `profession`, `gender`) VALUES(?, ?, ?, ?, ?)")
                            statement.setLong(1, this.connection.aid)
                            statement.setLong(2, this.connection.authSession.accountId)
                            statement.setString(3, this.connection.authSession.charName)
                            statement.setString(4, this.connection.authSession.charProfession)
                            statement.setString(5, this.connection.authSession.charGender)
                            statement.executeUpdate()
                        }
                    }

                    data = server.databaseManager.playerDataCache.loadOne(connection.aid)

                    server.ticker.registerWaitable { this.handleNewPlayer(data!!) }.wait()
                }
                else
                {
                    server.ticker.registerWaitable { this.handleOnlinePlayer() }.wait()
                }

                server.ticker.registerWaitable { this.handleInit(out) }.wait()
            }
            2 -> // collisions
            {
                server.ticker.registerWaitable {
                    out.json.addProperty("cl", (this.player!!.location.town!! as TownImpl).cachedMapData.collisionString)
                    connection.initLevel = 2
                }.wait()
            }
            3 -> // items
            {
                server.ticker.registerWaitable { this.initItems() }.wait()
            }
            4 -> // finish
            {
                server.chatManager.getPlayerInitMessages(this.player!!).forEach { out.addChatMessage(it) }
                out.addEvent()
                connection.initLevel = 4
            }
        }

        out.markAsOk()
        return true
    }

    private fun handleNewPlayer(data: PlayerDataImpl)
    {
        val player = PlayerImpl(data, this.server, this.connection)

        data.player_ = player
        data.inventory!!.player_ = player
        this.connection.player = player

        this.server.entityManager.registerEntity(this.connection.player!!)

        // spawn if its a fresh registered player
        val location = player.location
        if (location.town == null)
        {
            location.town = this.server.parsedGameData.professionRespawnMap[data.profession]
            val town = location.town as TownImpl
            location.x = town.cachedMapData.spawnPoint.x
            location.y = town.cachedMapData.spawnPoint.y
        }

        // give default bag if the player doesn't have any
        val inventory = data.inventory!!
        var hasAnyBag = false
        for (i in 0..3)
        {
            if (inventory.getBag(i) != null)
            {
                hasAnyBag = true
            }
        }

        if (!hasAnyBag)
        {
            val defaultBag = this.server.itemManager.newItemStack(this.server.parsedGameData.defaultBag)
            inventory.setBag(0, defaultBag)
        }

        // call events and internal stuff
        player.server.eventManager.call(PlayerJoinEvent(player))
        player.connected()
    }

    private fun handleOnlinePlayer()
    {
        val player = this.player!!
        player.entityTracker.reset()

        val tracker = player.itemTracker
        tracker.enabled = false
        tracker.reset()

        player.currentNpcTalk?.needsUpdate = true
        player.battleData?.reset()
    }

    private fun handleInit(out: OutgoingPacket)
    {
        val j = out.json
        val player = this.player!!

        val town = player.location.town!! as TownImpl

        j.add("town", town.cachedMapData.townElement)

        j.add("gw2", town.cachedMapData.gw2)
        j.add("townname", town.cachedMapData.townname)

        j.addProperty("worldname", this.server.config.serverConfig!!.name)
        j.addProperty("time", TimeUtils.getTimestampLong())
        j.addProperty("tutorial", -1)
        j.addProperty("clientver", 1461248638)

        if (!player.initialized)
        {
            j.add("h", GsonUtils.gson.toJsonTree(player.data.recalculateStatistics(StatisticType.ALL)))
            player.initialized = true
        }

        out.addStatisticRecalculation(StatisticType.ALL)

        connection.initLevel = 1
    }

    private fun initItems()
    {
        val tracker = this.player!!.itemTracker
        tracker.enabled = true
        tracker.reset()
        tracker.doTrack()
        connection.initLevel = 3
    }
}