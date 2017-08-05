package pl.margoj.server.implementation.network.protocol.jsons

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class HeroObject(
        var id: Int? = null,
        var blockade: Int? = null,
        @SerializedName("uprawnienia")
        var permissions: Int? = null,
        var ap: Int? = null,
        var bagi: Int? = null,
        var bint: Int? = null,
        var bstr: Int? = null,
        var clan: Int? = null,
        var clanrank: Int? = null,
        var credits: Long? = null,
        var runes: Int? = null,
        var dir: Int? = null,
        var exp: Long? = null,
        var fgrp: Int? = null,
        var gold: Long? = null,
        var goldlim: Long? = null,
        var healpower: Int? = null,
        var honor: Int? = null,
        var img: String? = null,
        var lvl: Int? = null,
        var mails: Int? = null,
        @SerializedName("mails_all")
        var mailsAll: Int? = null,
        @SerializedName("mails_last")
        var mailsLast: String? = null,
        var mpath: String? = null,
        var nick: String? = null,
        var opt: Int? = null,
        var prof: Char? = null,
        var pttl: String? = null,
        var pvp: Int? = null,
        var ttl: Int? = null,
        var x: Int? = null,
        var y: Int? = null,
        var back: Int? = null,
        var bag: Int? = null,
        var party: Int? = null,
        var trade: Int? = null,
        var wanted: Int? = null,
        var stamina: Int? = null,
        @SerializedName("stamina_ts")
        var staminaTimestamp: Int? = null,
        @SerializedName("stamina_renew_sec")
        var staminaRenew: Int? = null,
        @SerializedName("warrior_stats")
        private var warriorStats_: WarriorStats? = null
)
{
    val warriorStats: WarriorStats
        get()
        {
            if (this.warriorStats_ == null)
            {
                this.warriorStats_ = WarriorStats()
            }

            return this.warriorStats_!!
        }
}

data class WarriorStats(
        var hp: Int? = null,
        var maxhp: Int? = null,
        var st: Int? = null,
        var ag: Int? = null,
        var it: Int? = null,
        var sa: BigDecimal? = null,
        var crit: BigDecimal? = null,
        var ac: Int? = null,
        var resfire: Int? = null,
        var resfrost: Int? = null,
        var reslight: Int? = null,
        var act: Int? = null,
        var dmg: Int? = null,
        var dmgc: Int? = null,
        var critmval: BigDecimal? = null,
        var critmval_f: BigDecimal? = null,
        var critmval_c: BigDecimal? = null,
        var critmval_l: BigDecimal? = null,
        var evade: Int? = null,
        var lowcrit: Int? = null,
        @SerializedName("blok")
        var block: Int? = null,
        var mana: Int? = null,
        var acmdmg: Int? = null,
        var energy: Int? = null,
        var lowevade: Int? = null
)