package pl.margoj.server.implementation.chat

import org.apache.commons.lang3.StringEscapeUtils
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import pl.margoj.server.api.chat.ChatManager
import pl.margoj.server.api.chat.ChatMessage
import pl.margoj.server.api.events.player.PlayerChatEvent
import pl.margoj.server.api.utils.TimeUtils
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.map.TownImpl
import pl.margoj.server.implementation.player.PlayerImpl
import java.util.concurrent.TimeUnit

class ChatManagerImpl(val server: ServerImpl) : ChatManager
{
    private val logger = LogManager.getLogger("Chat")

    fun handle(player: PlayerImpl, input: String)
    {
        val escapedInput = StringEscapeUtils.escapeHtml4(input)

        val event = PlayerChatEvent(player, escapedInput)
        server.eventManager.call(event)
        if (event.cancelled)
        {
            return
        }

        this.server.gameLogger.info("${player.name}: chat input: $input")

        when
        {
            input.startsWith("/k ") -> this.handleClanChat(player, input.substring(3))
            input.startsWith("/g ") -> this.handleGroupChat(player, input.substring(3))
            input.startsWith("@") -> this.handlePrivateChat(player, input.substring(1))
            else -> this.handleNormalChat(player, input)
        }
    }

    fun getPlayerInitMessages(player: PlayerImpl): Collection<ChatMessage>
    {
        val townHistory = (player.location.town as TownImpl).chatHistory

        val oldestMessageTimestamp = TimeUtils.getTimestampDouble() - TimeUnit.HOURS.toSeconds(1L).toDouble()
        while(townHistory.isNotEmpty() && townHistory.first.timestamp.toDouble() < oldestMessageTimestamp)
        {
            townHistory.removeFirst()
        }

        val history = ArrayList<ChatMessage>(player.chatHistory.size + townHistory.size)
        history.addAll(player.chatHistory)
        townHistory.stream().filter {  !history.contains(it) }.forEach { history.add(it.copy(style = "abs")) }

        return history
    }

    private fun handleNormalChat(player: PlayerImpl, message: String)
    {
        val town = player.location.town as TownImpl

        this.logger.info("Mapa[${town.id}, ${town.name}] ${player.name}: $message")

        val chatMessage = ChatMessage(text = message, nickname = player.name)

        town.chatHistory.add(chatMessage)
        if(town.chatHistory.size > 20)
        {
            town.chatHistory.removeFirst()
        }

        for (target in this.server.players)
        {
            if(target.location.town === town)
            {
                target.sendChatMessage(chatMessage)
            }
        }
    }

    private fun handleClanChat(player: PlayerImpl, message: String)
    {
        player.displayAlert("Aby używać tego chatu, musisz należeć do jakiegoś klanu!") // TODO
    }

    private fun handleGroupChat(player: PlayerImpl, message: String)
    {
        player.displayAlert("Aby używać tego chatu, musisz należeć do jakiejś drużyny!") // TODO
    }

    private fun handlePrivateChat(player: PlayerImpl, message: String)
    {
        val split = message.split(" ")
        if(split.size < 2)
        {
            this.sendSystemMessage(player, "Aby wysłać wiadomość prywatną wpisz: @nick_gracza treść.")
            return
        }

        val target = this.server.getPlayer(split[0])
        if(target == null || !target.online)
        {
            this.sendSystemMessage(player, "Gracz nie istnieje!")
            return
        }

        val chatMessage = ChatMessage(
                text = StringUtils.join(split.toTypedArray(), " ", 1, split.size),
                type = 3,
                nickname = player.name,
                target = target.name,
                style = "priv"
        )

        this.logger.info("Prywatny[${player.name}->${target.name}] ${chatMessage.text}")

        player.sendChatMessage(chatMessage)
        target.sendChatMessage(chatMessage)
    }

    private fun sendSystemMessage(player: PlayerImpl, message: String)
    {
        player.sendChatMessage(ChatMessage(
                text = message,
                type = 3,
                nickname = "System",
                target = player.name,
                style = "sys_info"
        ))
    }
}