package pl.margoj.server.implementation.network.protocol.jsons

import com.google.gson.annotations.SerializedName
import pl.margoj.server.api.player.Profession

data class OtherObject(
        @Transient var id: Int = 0,
        var nick: String? = null,
        var icon: String? = null,
        var clan: String? = null,
        var x: Int? = null,
        var y: Int? = null,
        @SerializedName("dir")
        var direction: Int? = null,
        var rights: Int? = null,
        @SerializedName("lvl")
        var level: Int? = null,
        @SerializedName("prof")
        var profession: Profession? = null,
        @SerializedName("attr")
        var attributes: Int? = null,
        var relation: String? = null,
        var del: Int? = null
)