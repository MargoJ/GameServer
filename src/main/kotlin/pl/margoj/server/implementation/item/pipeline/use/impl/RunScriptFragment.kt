package pl.margoj.server.implementation.item.pipeline.use.impl

import pl.margoj.mrf.item.ItemCategory
import pl.margoj.mrf.item.ItemProperties
import pl.margoj.server.implementation.item.pipeline.use.ItemUsePipelineData
import pl.margoj.server.implementation.item.pipeline.use.ItemUsePipelineFragment
import pl.margoj.server.implementation.npc.NpcTalk

class RunScriptFragment : ItemUsePipelineFragment
{
    override fun process(fragment: ItemUsePipelineData)
    {
        val item = fragment.item
        val player = fragment.user

        if (item[ItemProperties.CATEGORY] == ItemCategory.CONSUMABLE)
        {
            val runScript = item[ItemProperties.RUN_SCRIPT]

            if (runScript.isNotEmpty())
            {
                val script = player.server.npcScriptParser.getNpcScript(runScript)
                val talk = NpcTalk(player, null, script!!)
                player.currentNpcTalk = talk

                fragment.take = true
                fragment.putOnCooldown = true
            }
        }
    }
}