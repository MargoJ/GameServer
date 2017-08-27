package pl.margoj.server.implementation.utils

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import pl.margoj.server.api.player.Gender
import pl.margoj.server.api.player.Profession
import pl.margoj.server.api.utils.CharEnumJsonAdapter

internal object GsonUtils
{
    val gson = addSerializers(GsonBuilder()).disableHtmlEscaping().create()!!
    val prettyGson = addSerializers(GsonBuilder()).disableHtmlEscaping().setPrettyPrinting().create()!!
    val parser = JsonParser()

    fun addSerializers(builder: GsonBuilder): GsonBuilder
    {
        builder.registerTypeAdapter(Profession::class.java, CharEnumJsonAdapter.simple({ it.id }, { Profession.getById(it) }))
        builder.registerTypeAdapter(Gender::class.java,  CharEnumJsonAdapter.simple({ it.id }, { Gender.getById(it) }))
        return builder
    }
}