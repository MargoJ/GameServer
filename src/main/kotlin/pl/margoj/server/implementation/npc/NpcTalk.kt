package pl.margoj.server.implementation.npc

import com.google.gson.JsonArray
import pl.margoj.server.api.player.Player
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.npc.parser.CodeLabel
import pl.margoj.server.implementation.npc.parser.Label
import pl.margoj.server.implementation.npc.parser.SystemLabel
import pl.margoj.server.implementation.npc.parser.buildin.PlayerBuildInVariable
import pl.margoj.server.implementation.npc.parser.buildin.ServerBuildInVariable
import pl.margoj.server.implementation.npc.parser.parsed.NpcParsedScript
import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext
import pl.margoj.server.implementation.player.PlayerImpl

class NpcTalk(val player: Player, val npc: Npc?, val npcScript: NpcParsedScript)
{
    var name: String? = null
    var text: String? = null
    val options: MutableList<Option> = ArrayList(5)
    var finished = false
        private set

    private var optionId = 1
    private var any = false

    val context = ScriptContext(player, npc)
    var needsUpdate: Boolean = true

    init
    {
        context.setVariable("gracz", PlayerBuildInVariable(this.player as PlayerImpl))
        context.setVariable("serwer", ServerBuildInVariable(this.player.server))

        this.update(CodeLabel("start"), null)
    }

    fun update(label: Label, parameters: Array<Any>?)
    {
        this.text = ""
        this.options.clear()

        context.delegate = this::delegate
        context.nextLabel = this::nextLabel

        if (parameters != null)
        {
            var i = 0
            while (i < parameters.size)
            {
                context.setVariable("argument${i + 1}", parameters[i])
                i++
            }
        }

        context.nextLabel!!.invoke(label, context)

        this.needsUpdate = true
    }

    private fun nextLabel(label: Label, context: ScriptContext)
    {
        if (label is CodeLabel)
        {
            this.npcScript.getNpcCodeBlock(label.name)!!.execute(context)
        }
        else if (label is SystemLabel)
        {
            this.executeSystemLabel(label)
        }
    }

    private fun delegate(function: String, parameters: Array<Any>, context: ScriptContext)
    {
        when (function)
        {
            "nazwa" ->
            {
                this.name = parameters[0] as String
            }
            "dialog" ->
            {
                this.text += (parameters[0] as String) + "<br>"
            }
            "opcja" ->
            {
                val label = parameters[1] as Label

                val labelParameters: Array<Any> = if (parameters.size <= 2)
                {
                    EMPTY_ANY_ARRAY
                }
                else
                {
                    Array(parameters.size - 2) { i -> parameters[2 + i] }
                }

                this.options.add(Option(optionId++, label.type, parameters[0] as String, label, labelParameters))
            }
        }
    }

    private fun executeSystemLabel(label: SystemLabel)
    {
        return when (label.name)
        {
            "koniec", "zakończ" -> this.finished = true
            else -> throw IllegalStateException("invalid label $label")
        }
    }

    fun handlePacket(out: OutgoingPacket)
    {
        if (this.name == null || this.text == null)
        {
            this.finished = true

            if (!this.any)
            {
                return
            }
        }

        val array = JsonArray()

        fun addElement(type: Int, first: String, second: String)
        {
            array.add(type.toString())
            array.add(first)
            array.add(second)
        }

        if (!finished)
        {
            addElement(0, this.name!!, npc?.id?.toString() ?: "0")
            addElement(1, this.text!!, "")

            for ((id, type, text) in this.options)
            {
                addElement(type, text, id.toString())
            }

            this.any = true
        }
        else
        {
            addElement(4, "", "")
        }

        out.json.add("d", array)
    }

    private companion object
    {
        private val EMPTY_ANY_ARRAY = arrayOf<Any>()
    }
}

data class Option(val id: Int, val type: Int, val text: String, val label: Label, val parameters: Array<Any>)