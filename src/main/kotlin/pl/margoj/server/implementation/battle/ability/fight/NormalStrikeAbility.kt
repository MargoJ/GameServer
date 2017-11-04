package pl.margoj.server.implementation.battle.ability.fight

import pl.margoj.server.implementation.battle.BattleImpl
import pl.margoj.server.implementation.entity.LivingEntityImpl

class NormalStrikeAbility(battle: BattleImpl, user: LivingEntityImpl, target: LivingEntityImpl) : StrikeAbility(battle, user, target)
{

}