package pl.margoj.server.implementation.network.protocol.jsons

import com.google.gson.annotations.SerializedName

data class NpcObject(
        @Transient var id: Int = 0,
        var nick: String? = null,
        @SerializedName("qm")
        var questMark: Int? = null,
        var icon: String? = null,
        var x: Int? = null,
        var y: Int? = null,
        @SerializedName("lvl")
        var level: Int? = null,
        var type: Int? = null,
        @SerializedName("wt")
        var subType: Int? = null,
        @SerializedName("grp")
        var group: Int? = null
)