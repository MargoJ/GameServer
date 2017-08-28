package pl.margoj.server.implementation.npc

import pl.margoj.server.api.battle.BattleStats
import pl.margoj.server.api.player.Profession

class NpcData(val npc: Npc) : BattleStats
{
    override var level: Int = 0

    /// TODO!
    override var profession: Profession = Profession.WARRIOR
    override var strength: Int = 4
    override var agility: Int = 3
    override var intellect: Int = 3
    override var attackSpeed: Double = 1.0
    override var maxHp: Int = 1000
}