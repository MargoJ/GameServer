package pl.margoj.server.implementation.network.protocol.jsons

import com.google.gson.annotations.SerializedName

data class ItemObject(
        var id: Long? = 0,
        var hid: Long? = null,
        var name: String? = null,
        var own: Int? = null,
        @SerializedName("loc")
        var location: String? = null,
        var icon: String? = null,
        var x: Int? = null,
        var y: Int? = null,
        @SerializedName("cl")
        var itemCategory: Int? = null,
        @SerializedName("pr")
        var price: Long? = null,
        @SerializedName("st")
        var slot: Int? = null,
        @SerializedName("stat")
        var statistics: String? = null,
        @SerializedName("del")
        var delete: Int? = null
)