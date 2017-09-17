package pl.margoj.server.implementation.player.sublisteners

import com.google.gson.JsonArray
import pl.margoj.server.implementation.network.protocol.IncomingPacket
import pl.margoj.server.implementation.network.protocol.OutgoingPacket
import pl.margoj.server.implementation.player.PlayerConnection
import pl.margoj.server.implementation.player.PlayerImpl
import pl.margoj.server.implementation.skills.SkillOutObject
import pl.margoj.server.implementation.skills.SkillSetId
import java.util.concurrent.ThreadLocalRandom

class PlayerSkillsListener(connection: PlayerConnection) : PlayerPacketSubListener(connection, onlyOnPlayer = true)
{
    override fun handle(packet: IncomingPacket, out: OutgoingPacket, query: Map<String, String>): Boolean
    {
        val player = this.player!!

        if (player.inActiveBattle)
        {
            return true
        }

        when (packet.type)
        {
            "skills" -> this.handleSkills(player, out, packet)
            "skillshop" -> this.handleSkillShop(player, out, packet)
        }

        return true
    }

    private fun handleSkills(player: PlayerImpl, out: OutgoingPacket, packet: IncomingPacket)
    {
        val set = packet.queryParams["set"]

        if (set != null)
        {
            val setSkillId = SkillSetId.fromNumber(set.toIntOrNull())
            this.checkForMaliciousData(setSkillId == null, "Invalid skillset")

            player.data.playerOptions.skillSetOption.value = setSkillId!!
        }
    }

    private fun handleSkillShop(player: PlayerImpl, out: OutgoingPacket, packet: IncomingPacket)
    {
        val skillsArray = JsonArray()

        var testId = 0

        for (group in 9..15)
        {
            for (i in 0 until 6)
            {
                SkillOutObject(
                        skillId = ++testId,
                        skillName = "Test Skill $testId",
                        group = group,
                        isActive = true,
                        description = "Testowy Opis",
                        requirements = "",
                        currentLevel = 5,
                        maxLevel = 10,
                        statistics = "pdmg_physical-perw=" + ThreadLocalRandom.current().nextInt(0, 100),
                        nextLevelRequirements = "lvl=${(group - 8) * 25}",
                        nextLevelStats = ""
                ).appendToArray(skillsArray)
            }
        }

        out.json.addProperty("skill_set", player.data.playerOptions.skillSetOption.value.number) // never used in client, garmory server still sends it though
        out.json.add("skill_list", skillsArray)
        out.json.addProperty("skills_learnt", 123)
    }
}