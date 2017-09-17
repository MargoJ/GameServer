package pl.margoj.server.implementation.player.options

import pl.margoj.server.implementation.skills.SkillSetId

abstract class PlayerOptionsProperties<T>(protected val playerOptions: PlayerOptions, protected val bit: Int)
{
    protected open var value_: Boolean
        set(value)
        {
            this.playerOptions.needsUpdate = true

            if(value)
            {
                this.playerOptions.intValue = this.playerOptions.intValue or this.bit
            }
            else
            {
                this.playerOptions.intValue = this.playerOptions.intValue and (this.bit.inv())
            }
        }
        get()
        {
            return (this.playerOptions.intValue and this.bit) != 0
        }

    abstract var value: T
}

class SkillSetOptionProperty(playerOptions: PlayerOptions, bit: Int) : PlayerOptionsProperties<SkillSetId>(playerOptions, bit)
{
    override var value: SkillSetId
        get()
        {
            return if(this.value_) SkillSetId.SKILL_II else SkillSetId.SKILL_I
        }
        set(value)
        {
            this.value_ = value == SkillSetId.SKILL_II
        }
}