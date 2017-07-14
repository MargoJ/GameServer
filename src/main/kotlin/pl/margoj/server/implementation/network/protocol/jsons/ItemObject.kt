package pl.margoj.server.implementation.network.protocol.jsons

import com.google.gson.annotations.SerializedName

data class ItemObject(
        var id: Int = 0,
        var hid: Int? = null,
        var name: String? = null,
        var own: Int = 0,
        @SerializedName("loc")
        var location: String? = null,
        var icon: String? = null,
        var x: Int = 0,
        var y: Int = 0,
        @SerializedName("cl")
        var itemType: Int = 0,
        @SerializedName("pr")
        var price: Int = 0,
        @SerializedName("st")
        var slot: Int = 0,
        @SerializedName("stat")
        var statistics: String? = null
)