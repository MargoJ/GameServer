package pl.margoj.server.implementation.player

import pl.margoj.server.api.player.PlayerData
import pl.margoj.server.api.player.Profession
import pl.margoj.server.implementation.network.protocol.jsons.HeroObject

// TODO
class PlayerDataImpl(val player: PlayerImpl) : PlayerData
{
    override var level: Int = 500

    override var xp: Long = (Math.pow((this.level - 1).toDouble(), 4.toDouble())).toLong() + 10L

    override var icon: String = "/vip/3599584.gif"

    override val profession: Profession = Profession.PALADIN

    fun createHeroObject(): HeroObject
    {
        return HeroObject(
                id = this.player.connection.aid,
                blockade = 0,
                permissions = 0,
                ap = 0,
                bagi = 3,
                bint = 3,
                bstr = 4,
                clan = 0,
                clanrank = 0,
                credits = 0,
                runes = 0,
                dir = this.player.movementManager.playerDirection,
                exp = this.xp,
                fgrp = 0,
                gold = this.player.currencyManager.gold,
                goldlim = this.player.currencyManager.goldLimit,
                healpower = 0,
                honor = 0,
                img = this.icon,
                lvl = this.level,
                mails = 0,
                mailsAll = 0,
                mailsLast = "",
                mpath = "",
                nick = this.player.name,
                opt = 0,
                prof = this.profession.id,
                pttl = "Limit 6h/dzień",
                pvp = 0,
                ttl = 275,
                x = this.player.location.x,
                y = this.player.location.y,
                bag = 0,
                party = 0,
                trade = 0,
                wanted = 0,
                stamina = 50,
                staminaTimestamp = 0,
                staminaRenew = 0,
                st = 4,
                ag = 3,
                it = 3,
                dmg = "3",
                ac = 0,
                act = 0,
                resis = "0/0/0",
                sa = 1.06,
                hp = 40,
                heal = 0,
                maxhp = 40,
                crit = 1.02,
                critval = 1.20,
                critmval = 1.20,
                critmval2 = "1.2,1.2,1.2",
                ofCrit = 0.00,
                ofCritval = 1.20,
                evade = 0,
                absorb =  0,
                absorbm = 0,
                block = 0,
                mana = 0,
                energy = 50
        )
    }
}