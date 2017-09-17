package pl.margoj.server.implementation.skills

import com.google.gson.JsonArray

data class SkillOutObject(
        val skillId: Int,
        val skillName: String,
        val isActive: Boolean,
        val group: Int,
        val description: String,
        val requirements: String,
        val currentLevel: Int,
        val maxLevel: Int,
        val statistics: String,
        val nextLevelRequirements: String? = null,
        val nextLevelStats: String? = null
)
{
    fun appendToArray(array: JsonArray)
    {
        array.add(this.skillId)
        array.add(this.skillName)

        var attr = 0
        if (this.isActive)
        {
            attr = attr or 1
        }
        array.add(attr)

        array.add(this.group)
        array.add(0) // xy, appears to be unused ?
        array.add(this.description)
        array.add(this.requirements)
        array.add("${this.currentLevel}/${this.maxLevel}")
        array.add(this.statistics)

        if (this.nextLevelRequirements == null || this.nextLevelStats == null)
        {
            array.add("unav")
        }
        else
        {
            array.add("${this.nextLevelRequirements}|${this.nextLevelStats}")
        }
    }
}