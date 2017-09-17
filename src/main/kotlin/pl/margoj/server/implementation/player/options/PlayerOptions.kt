package pl.margoj.server.implementation.player.options

class PlayerOptions(intValue: Int)
{
    internal var needsUpdate = false

    var intValue = intValue
        internal set

    val skillSetOption = SkillSetOptionProperty(this, 2 shl 9)
}