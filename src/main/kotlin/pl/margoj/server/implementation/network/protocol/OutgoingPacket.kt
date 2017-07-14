package pl.margoj.server.implementation.network.protocol

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import pl.margoj.server.api.chat.ChatMessage
import pl.margoj.server.api.utils.TimeUtils
import pl.margoj.server.implementation.network.protocol.jsons.ItemObject
import pl.margoj.server.implementation.network.protocol.jsons.OtherObject
import pl.margoj.server.implementation.utils.GsonUtils
import java.math.BigDecimal

class OutgoingPacket
{
    val json = com.google.gson.JsonObject()
    var shouldStop = false
        private set
    var raw: String? = null
    private val messages = mutableListOf<ChatMessage>()
    private val others = mutableListOf<OtherObject>()
    private val items = mutableListOf<ItemObject>()

    enum class EngineAction
    {
        STOP, RELOAD
    }

    fun addEngineAction(action: OutgoingPacket.EngineAction): OutgoingPacket
    {
        if (action == OutgoingPacket.EngineAction.STOP)
        {
            this.shouldStop = true
        }
        this.json.addProperty("t", action.toString().toLowerCase())
        return this
    }

    fun addAlert(alert: String): OutgoingPacket
    {
        this.putStringLine("alert", alert)
        return this
    }

    fun addWarn(message: String): OutgoingPacket
    {
        this.putStringLine("w", message)
        return this
    }

    fun addEvent(value: Double = TimeUtils.getTimestampDouble()): OutgoingPacket
    {
        this.json.addProperty("ev", BigDecimal.valueOf(value))
        return this
    }

    fun addMove(x: Int, y: Int): OutgoingPacket
    {
        val h = getObject("h")
        h.addProperty("x", x)
        h.addProperty("y", y)
        return this
    }

    fun addItem(item: ItemObject): OutgoingPacket
    {
        this.items.add(item)
        return this
    }

    fun addJavascriptCode(js: String): OutgoingPacket
    {
        this.json.addProperty("js", js)
        return this
    }

    fun addChatMessage(chatMessage: ChatMessage): OutgoingPacket
    {
        this.messages.add(chatMessage)
        return this
    }

    fun markAsOk(): OutgoingPacket
    {
        this.json.addProperty("e", "ok")
        return this
    }

    fun addScreenMessage(message: String)
    {
        this.getArray("msg").add(message)
    }

    fun addOther(other: OtherObject): OutgoingPacket
    {
        this.others.add(other)
        return this
    }

    private fun <E : JsonElement> getJsonElement(name: String, constructor: () -> E, tester: (JsonElement) -> Boolean, transformer: (JsonElement) -> E): E
    {
        val firstTry = json.get(name)

        if (firstTry == null)
        {
            val new = constructor()
            json.add(name, new)
            return new
        }

        if (tester(firstTry))
        {
            return transformer(firstTry)
        }
        else
        {
            throw IllegalArgumentException("$name has invalid type")
        }
    }

    private fun assembleJson()
    {
        this.json.add("c", this.assembleListElement(this.messages, { i, _ -> i }, true))
        this.json.add("other", this.assembleListElement(this.others, { _, (id) -> id }))
        this.json.add("item", this.assembleListElement(this.items, { _, (id) -> id }))
    }

    private fun getObject(name: String): com.google.gson.JsonObject
    {
        return getJsonElement<JsonObject>(name, ::JsonObject, JsonElement::isJsonObject, JsonElement::getAsJsonObject)
    }

    private fun getArray(name: String): JsonArray
    {
        return getJsonElement<JsonArray>(name, ::JsonArray, JsonElement::isJsonArray, JsonElement::getAsJsonArray)
    }

    private fun <T> assembleListElement(list: List<T>, indexer: (Int, T) -> Int = { index, _ -> index }, invert: Boolean = false): JsonObject?
    {
        if (list.isEmpty())
        {
            return null
        }

        val objects = JsonObject()

        for ((i, o) in list.withIndex())
        {
            objects.add(indexer(if (invert) (list.size - i - 1) else i, o).toString(), GsonUtils.gson.toJsonTree(o).asJsonObject)
        }

        return objects
    }

    private fun putStringLine(name: String, input: String)
    {
        val element = this.json[name]

        if (element == null)
        {
            this.json.addProperty(name, input)
        }
        else if (!element.isJsonPrimitive || !element.asJsonPrimitive.isString)
        {
            throw IllegalArgumentException("Provided value is not a string")
        }
        else
        {
            this.json.addProperty(name, element.asString + "<br>" + input)
        }
    }

    override fun toString(): String
    {
        if (this.raw != null)
        {
            return this.raw!!
        }
        this.assembleJson()
        return GsonUtils.prettyGson.toJson(json)
    }
}