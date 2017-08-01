package pl.margoj.server.implementation.npc.talk

import com.google.gson.JsonArray
import pl.margoj.server.api.player.Player
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.npc.parser.CodeLabel
import pl.margoj.server.implementation.npc.parser.Label
import pl.margoj.server.implementation.npc.parser.SystemLabel
import pl.margoj.server.implementation.npc.parser.buildin.PlayerBuildInVariable
import pl.margoj.server.implementation.npc.parser.buildin.ServerBuildInVariable
import pl.margoj.server.implementation.npc.parser.parsed.NpcParsedScript
import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext
import pl.margoj.server.implementation.player.PlayerImpl

class NpcTalk(val player: Player, val npcScript: NpcParsedScript)
{
    lateinit var name: String
    lateinit var text: String
    val options: MutableList<Option> = ArrayList(5)
    var finished = false
        private set

    private var optionId = 1

    val context = ScriptContext(player)
    var needsUpdate: Boolean = true

    init
    {
        context.setVariable("gracz", PlayerBuildInVariable(this.player as PlayerImpl))
        context.setVariable("serwer", ServerBuildInVariable(this.player.server as ServerImpl))

        this.update(CodeLabel("start"))
    }

    fun update(label: Label)
    {
        this.text = ""
        this.options.clear()

        context.delegate = this::delegate

        if (label is CodeLabel)
        {
            this.npcScript.getNpcCodeBlock(label.name)!!.execute(context)
        }
        else if(label is SystemLabel)
        {
            this.executeSystemLabel(label)
        }

        this.needsUpdate = true
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
                var type = 2
                val label = parameters[1] as Label

                if (label is SystemLabel)
                {
                    type = this.getTypeForSystemLabel(label)
                }

                this.options.add(Option(optionId++, type, parameters[0] as String, label))
            }
        }
    }

    private fun getTypeForSystemLabel(label: SystemLabel): Int
    {
        return when (label.name)
        {
            "koniec" -> 2
            "zakończ" -> 6
            else -> throw IllegalStateException("invalid label $label")
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
        val array = JsonArray()

        fun addElement(type: Int, first: String, second: String)
        {
            array.add(type.toString())
            array.add(first)
            array.add(second)
        }

        if (!finished)
        {
            addElement(0, this.name, "0") // TODO: NPC ID
            addElement(1, this.text, "")

            for ((id, type, text) in this.options)
            {
                addElement(type, text, id.toString())
            }
        }
        else
        {
            addElement(4, "", "")
        }

        out.json.add("d", array)
    }
}

data class Option(val id: Int, val type: Int, val text: String, val label: Label)