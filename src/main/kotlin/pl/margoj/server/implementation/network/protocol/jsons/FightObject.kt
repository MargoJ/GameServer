package pl.margoj.server.implementation.network.protocol.jsons

import com.google.gson.annotations.SerializedName
import pl.margoj.server.api.player.Profession

data class FightObject
(
        var init: Int? = null,
        var auto: Int? = null,
        var battleGround: String? = null,
        var skills: Array<String>? = null,
        @SerializedName("w")
        var participants: Map<String, BattleParticipant>? = null,
        @SerializedName("mi")
        var battleLogOrder: Array<Int>? = null,
        @SerializedName("m")
        var battleLog: Array<String>? = null,
        @SerializedName("start_move")
        var startMove: Int? = null,
        var move: Int? = null,
        @SerializedName("myteam")
        var myTeam: Int? = null,
        var close: Int? = null
)

data class BattleParticipant
(
        var id: Long,
        var name: String? = null,
        @SerializedName("lvl")
        var level: Int? = null,
        @SerializedName("prof")
        var profession: Profession? = null,
        var npc: Int? = null,
        var gender: String? = null,
        @SerializedName("hpp")
        var healthPercent: Int? = null,
        var team: Int? = null,
        @SerializedName("y")
        var row: Int? = null,
        var mana: Int? = null,
        var energy: Int? = null,
        var fast: Int? = null,
        var icon: String? = null,
        var buffs: Int? = null
)