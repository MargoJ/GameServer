package pl.margoj.server.implementation.network.http

import java.util.LinkedHashMap

/**
 * Edited version of a nety class
 */
class NoSemicolonSingleValueQueryStringDecoder(val uri: String)
{

    private var parameters_: Map<String, String>? = null

    var parameters: Map<String, String>
        set(value)
        {
            parameters_ = value
        }
        get()
        {
            if (parameters_ == null)
            {
                val pathLength = path.length
                if (uri.length == pathLength)
                {
                    return mutableMapOf()
                }
                decodeParams(uri.substring(pathLength + 1))
            }
            return parameters_!!
        }

    var path: String = ""
        private set
        get()
        {
            if (field.isEmpty())
            {
                val pathEndPos = uri.indexOf('?')
                if (pathEndPos < 0)
                {
                    field = uri
                }
                else
                {
                    field = uri.substring(0, pathEndPos)
                }
            }
            return field
        }


    private fun decodeParams(s: String)
    {
        val params = LinkedHashMap<String, String>()
        this.parameters = params

        var name: String? = null
        var pos = 0
        var i = 0
        var c: Char

        while (i < s.length)
        {
            c = s[i]
            if (c == '=' && name == null)
            {
                if (pos != i)
                {
                    name = decodeComponent(s.substring(pos, i))
                }
                pos = i + 1
            }
            else if (c == '&')
            {
                if (name == null && pos != i)
                {
                    params.put(decodeComponent(s.substring(pos, i)), "")
                }
                else if (name != null)
                {
                    params.put(name, decodeComponent(s.substring(pos, i)))
                    name = null
                }
                pos = i + 1
            }
            i++
        }

        if (pos != i)
        {
            if (name == null)
            {
                params.put(decodeComponent(s.substring(pos, i)), "")
            }
            else
            {
                params.put(name, decodeComponent(s.substring(pos, i)))
            }
        }
        else if (name != null)
        {
            params.put(name, "")
        }
    }

    fun decodeComponent(s: String?): String
    {
        if (s == null)
        {
            return ""
        }
        val size = s.length
        val modified = (0 until size)
                .map { s[it] }
                .any { it == '%' || it == '+' }
        if (!modified)
        {
            return s
        }
        val buf = ByteArray(size)
        var pos = 0
        var i = 0
        loop@
        while (i < size)
        {
            var c = s[i]
            when (c)
            {
                '+' -> buf[pos++] = ' '.toByte()
                '%' ->
                {
                    if (i == size - 1)
                    {
                        throw IllegalArgumentException("unterminated escape" + " sequence at end of string: " + s)
                    }
                    c = s[++i]
                    if (c == '%')
                    {
                        buf[pos++] = '%'.toByte()
                        i++
                        continue@loop
                    }
                    if (i == size - 1)
                    {
                        throw IllegalArgumentException("partial escape" + " sequence at end of string: " + s)
                    }
                    c = decodeHexNibble(c)
                    val c2 = decodeHexNibble(s[++i])
                    if (c == Character.MAX_VALUE || c2 == Character.MAX_VALUE)
                    {
                        throw IllegalArgumentException("invalid escape sequence `%" + s[i - 1] + s[i] + "' at index " + (i - 2) + " of: " +
                                s)
                    }
                    c = (c.toInt() * 16 + c2.toInt()).toChar()
                    buf[pos++] = c.toByte()
                }
                else -> buf[pos++] = c.toByte()
            }
            i++
        }
        return String(buf, 0, pos)
    }

    private fun decodeHexNibble(c: Char): Char
    {
        when (c)
        {
            in '0'..'9' -> return (c - '0').toChar()
            in 'a'..'f' -> return (c - 'a' + 10).toChar()
            in 'A'..'F' -> return (c - 'A' + 10).toChar()
            else -> return Character.MAX_VALUE
        }
    }
}