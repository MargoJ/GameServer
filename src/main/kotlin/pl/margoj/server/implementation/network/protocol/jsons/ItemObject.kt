package pl.margoj.server.implementation.network.protocol.jsons

import com.google.gson.annotations.SerializedName

data class ItemObject(
        var hid: Int = 0,
        var name: String? = null,
        var own: Int = 0,
        @SerializedName("loc")
        var location: String? = null,
        var icon: String? = null,
        var x: Int = 0,
        var y: Int = 0,
        var cl: Int = 0,
        @SerializedName("pr")
        var price: Int = 0,
        var st: Int = 0,
        @SerializedName("stat")
        var statistics: String? = null
)