package pl.margoj.server.implementation.chat

import org.apache.commons.lang3.StringEscapeUtils
import pl.margoj.server.api.chat.ChatManager
import pl.margoj.server.api.chat.ChatMessage
import pl.margoj.server.api.events.player.PlayerChatEvent
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.player.PlayerImpl

class ChatManagerImpl(val server: ServerImpl) : ChatManager
{
    fun handle(player: PlayerImpl, input: String)
    {
        val escapedInput = StringEscapeUtils.escapeHtml4(input)

        val event = PlayerChatEvent(player, escapedInput)
        server.eventManager.call(event)
        if (event.cancelled)
        {
            return
        }

        val msg = ChatMessage(type = ChatMessage.Type.TYPE_GLOBAL, nickname = player.name, text = event.message)

        this.server.gameLogger.info("${player.name}: chat input: $input")
        this.server.players.filter { it.location.town == player.location.town }.forEach { it.sendChatMessage(msg) }
    }
}