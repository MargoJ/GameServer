package pl.margoj.server.implementation.player.sublisteners

import pl.margoj.server.implementation.battle.BattleData
import pl.margoj.server.implementation.network.protocol.IncomingPacket
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.network.protocol.jsons.BattleParticipant
import pl.margoj.server.implementation.network.protocol.jsons.FightObject
import pl.margoj.server.implementation.player.PlayerConnection
import pl.margoj.server.implementation.utils.GsonUtils
import java.util.LinkedList

class PlayerBattleSendListener(connection: PlayerConnection) : PlayerPacketSubListener(connection, onlyOnPlayer = true)
{
    override fun handle(packet: IncomingPacket, out: OutgoingPacket, query: Map<String, String>): Boolean
    {
        // make sure everything will be reset
        if (packet.type == "init" && packet.queryParams["initlvl"] == "1")
        {
            return true
        }

        val fightObject = FightObject()

        val player = this.player!!
        val battle = player.currentBattle

        if (battle != null)
        {
            val data = player.battleData!!

            // init if needed
            if (!data.initialized)
            {
                fightObject.init = 1
                fightObject.myTeam = if (data.team == BattleData.Team.TEAM_A) 1 else 2
                fightObject.battleGround = "aa1.jpg"
                fightObject.skills = emptyArray()

                data.initialized = true
            }

            // update if needed
            if (data.needsAutoUpdate)
            {
                fightObject.auto = if (data.auto) 1 else 0
                data.needsAutoUpdate = false
            }

            // update participants
            var participants: MutableList<BattleParticipant>? = null

            for (participant in battle.participants)
            {
                if (data.lastUpdateSendTick == -1L || participant.battleData!!.lastUpdatedTick > data.lastUpdateSendTick)
                {
                    if (participants == null)
                    {
                        participants = LinkedList()
                    }

                    if (participant.currentBattle == player.currentBattle)
                    {
                        participants.add(participant.battleData!!.createBattleParticipantObject(player))
                    }
                    else
                    {
                        participants.add(BattleParticipant(id = participant.id.toLong(), healthPercent = 0))
                    }
                }
            }

            if (participants != null)
            {
                val battleParticipants = HashMap<String, BattleParticipant>(participants.size)

                for (battleParticipant in participants)
                {
                    battleParticipants.put(battleParticipant.id.toString(), battleParticipant)
                }

                fightObject.participants = battleParticipants
            }

            data.lastUpdateSendTick = player.server.ticker.currentTick

            // send move
            if (battle.finished)
            {
                fightObject.move = -1
            }
            else if (battle.currentEntity == this.player)
            {
                fightObject.startMove = 15 // TODO
                fightObject.move = data.secondsLeft
            }
            else
            {
                fightObject.move = 0
            }

            // send log
            if (data.lastLog < battle.logCount - 1)
            {
                val log = ArrayList<Pair<Int, String>>(battle.logCount - data.lastLog)

                while (data.lastLog < battle.logCount - 1)
                {
                    val logFragment = battle.log[data.lastLog + 1]!!
                    log.add(Pair(data.lastLog + 1, logFragment))
                    data.lastLog++
                }

                if (log.isNotEmpty())
                {
                    fightObject.battleLogOrder = Array(log.size, { log[it].first })
                    fightObject.battleLog = Array(log.size, { log[it].second })
                }
            }
        }
        else if (player.battleData != null && player.battleData!!.quitRequested)
        {
            player.battleData!!.quitRequested = false
            fightObject.close = 1
        }
        else
        {
            return true
        }

        // send
        out.json.add("f", GsonUtils.gson.toJsonTree(fightObject))
        return true
    }
}