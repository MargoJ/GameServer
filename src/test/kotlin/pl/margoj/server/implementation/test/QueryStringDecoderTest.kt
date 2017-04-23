package pl.margoj.server.implementation.test

import org.junit.Assert
import org.junit.Test
import pl.margoj.server.implementation.network.http.NoSemicolonSingleValueQueryStringDecoder

class QueryStringDecoderTest
{
    @Test
    fun testQueryStringDecoder()
    {
        val decoder = NoSemicolonSingleValueQueryStringDecoder("https://margoj.pl/?test=values&and=some&more&multiple=5&multiple=7")

        Assert.assertEquals("path is invalid", "https://margoj.pl/", decoder.path)
        Assert.assertEquals("parameters count is invalid", 4, decoder.parameters.size)
        Assert.assertEquals("parametres are not parsed correctly", "values", decoder.parameters["test"])
        Assert.assertEquals("parametres are not parsed correctly", "some", decoder.parameters["and"])
        Assert.assertEquals("parametres are not parsed correctly", "", decoder.parameters["more"])
        Assert.assertEquals("parametres are not parsed correctly", "7", decoder.parameters["multiple"])

        val instance = decoder.parameters
        Assert.assertTrue("instances aren't same", instance === decoder.parameters)
    }
}