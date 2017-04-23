package pl.margoj.server.implementation.utils

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import pl.margoj.server.api.player.Profession

internal object GsonUtils
{
    val gson = addSerializers(GsonBuilder()).create()!!
    val prettyGson = addSerializers(GsonBuilder()).setPrettyPrinting().create()!!
    val parser = JsonParser()

    fun addSerializers(builder: GsonBuilder): GsonBuilder
    {
        builder.registerTypeAdapter(Profession::class.java, Profession.JsonAdapter())
        return builder
    }
}