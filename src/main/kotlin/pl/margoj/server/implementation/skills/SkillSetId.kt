package pl.margoj.server.implementation.skills

enum class SkillSetId(val number: Int)
{
    SKILL_I(1),
    SKILL_II(2);

    companion object
    {
        fun fromNumber(int: Int?): SkillSetId?
        {
            return when(int)
            {
                1 -> SKILL_I
                2 -> SKILL_II
                else -> null
            }
        }
    }
}