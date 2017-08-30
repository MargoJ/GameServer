package pl.margoj.server.implementation.commands.defaults.debug

import org.apache.commons.io.IOUtils
import pl.margoj.server.api.commands.Arguments
import pl.margoj.server.api.commands.CommandListener
import pl.margoj.server.api.commands.CommandSender
import pl.margoj.server.implementation.ServerImpl
import pl.margoj.server.implementation.npc.NpcTalk
import pl.margoj.server.implementation.player.PlayerImpl
import java.io.File
import java.io.FileInputStream
import java.nio.charset.StandardCharsets

class TestNpcCommand : CommandListener
{
    override fun commandPerformed(command: String, sender: CommandSender, args: Arguments)
    {
        args.ensureTrue({ sender is PlayerImpl }, "Tylko gracz może wykonać tą komende")

        val directory = File("testnpc")
        args.ensureTrue({ directory.exists() }, "Testowanie NPC jest wyłączone")

        if (!args.has(0))
        {
            sender.sendMessage("Dostępne skrypty: ")
            directory.listFiles()
                    .filter { it.extension == "mjn" }
                    .forEach { sender.sendMessage(it.nameWithoutExtension) }
            return
        }

        val file = File(directory, args.asString(0) + ".mjn")
        args.ensureTrue({ file.exists() }, "Nieznany skrypt: ${args.asString(0)}")

        var content: String? = null

        FileInputStream(file).use {
            content = String(IOUtils.toByteArray(it), StandardCharsets.UTF_8)
        }

        val server = sender.server as ServerImpl

        val start = System.currentTimeMillis()
        val script = server.npcScriptParser.parse(file.nameWithoutExtension, content!!)
        val end = System.currentTimeMillis()
        sender.sendMessage("Skrypt sparsowany w ${end - start} ms")

        if (!args.has(1))
        {
            sender.sendMessage("Dostepne npc: ")
            script.allScripts.forEach { sender.sendMessage(it.name) }
            return
        }

        val npcScript = script.getNpcScript(args.asString(1))
        args.ensureNotNull(npcScript, "Nieznany npc: ${args.asString(1)}")

        sender.sendMessage("Wysłam dialog npc '${npcScript!!.name}'!")

        val talk = NpcTalk(sender as PlayerImpl, null, npcScript)
        sender.currentNpcTalk = talk
    }
}