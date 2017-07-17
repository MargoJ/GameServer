package pl.margoj.server.implementation.network.protocol.jsons

import com.google.gson.annotations.SerializedName

data class ItemObject(
        var id: Long? = 0,
        var hid: Long? = null,
        var name: String? = null,
        var own: Int? = null, // TODO
        @SerializedName("loc")
        var location: String? = null, // TODO
        var icon: String? = null,
        var x: Int? = null, // TODO
        var y: Int? = null, // TODO
        @SerializedName("cl")
        var itemCategory: Int? = null,
        @SerializedName("pr")
        var price: Long? = null,
        @SerializedName("st")
        var slot: Int? = null, // TODO
        @SerializedName("stat")
        var statistics: String? = null
)