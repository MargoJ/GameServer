package pl.margoj.server.implementation.item.pipeline.use.impl

import pl.margoj.mrf.item.ItemProperties
import pl.margoj.server.api.inventory.player.ItemRequirementsNotMetException
import pl.margoj.server.api.player.Profession
import pl.margoj.server.implementation.item.pipeline.use.ItemUsePipelineData
import pl.margoj.server.implementation.item.pipeline.use.ItemUsePipelineFragment

class RequirementsFragment : ItemUsePipelineFragment
{
    override fun process(fragment: ItemUsePipelineData)
    {
        val item = fragment.item

        if (!item[ItemProperties.CATEGORY].usable)
        {
            fragment.canceled = true
            return
        }

        val levelRequirement = item[ItemProperties.LEVEL_REQUIREMENT]
        val levelRequirementMet = levelRequirement == 0 || fragment.user.level >= levelRequirement

        val professionRequirement = item[ItemProperties.PROFESSION_REQUIREMENT]
        var professionRequirementMet = true

        if (professionRequirement.any)
        {
            professionRequirementMet = when (fragment.user.stats.profession)
            {
                Profession.WARRIOR -> professionRequirement.warrior
                Profession.PALADIN -> professionRequirement.paladin
                Profession.BLADE_DANCER -> professionRequirement.bladedancer
                Profession.MAGE -> professionRequirement.mage
                Profession.HUNTER -> professionRequirement.hunter
                Profession.TRACKER -> professionRequirement.tracker
            }
        }

        if (!levelRequirementMet || !professionRequirementMet)
        {
            throw ItemRequirementsNotMetException(!levelRequirementMet, !professionRequirementMet)
        }
    }
}