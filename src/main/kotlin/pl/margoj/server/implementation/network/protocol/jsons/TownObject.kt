package pl.margoj.server.implementation.network.protocol.jsons

import com.google.gson.annotations.SerializedName

data class TownObject(
        @SerializedName("id")
        var mapId: Int = 0,
        @SerializedName("mainid")
        var mainMapId: Int = 0,
        @SerializedName("x")
        var width: Int = 0,
        @SerializedName("y")
        var height: Int = 0,
        @SerializedName("file")
        var imageFileName: String? = null,
        @SerializedName("name")
        var mapName: String? = null,
        var pvp: Int = 0,
        var water: String? = null,
        @SerializedName("bg")
        var battleBackground: String? = null,
        @SerializedName("welcome")
        var welcomeMessage: String? = null
)