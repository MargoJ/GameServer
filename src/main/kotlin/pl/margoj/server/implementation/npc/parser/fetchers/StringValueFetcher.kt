package pl.margoj.server.implementation.npc.parser.fetchers

import org.apache.commons.lang3.StringUtils
import pl.margoj.server.implementation.npc.parser.ValueFetcher
import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext
import pl.margoj.server.implementation.npc.parser.statements.VariableSetStatement

class StringValueFetcher : ValueFetcher<String>()
{
    override val type: Class<String> = String::class.java

    override fun fetch(context: ScriptContext, value: String): String?
    {
        if (value.length <= 1 || !StringUtils.contains(value, '!'.toInt()))
        {
            return value
        }

        val array = value.toCharArray()
        val out = StringBuilder()

        var i = 0
        while (i < array.size)
        {
            if (array[i] == '!')
            {
                if (i > 0 && array[i - 1] == '\\')
                {
                    out.deleteCharAt(out.length - 1)
                    out.append(array[i])
                    i++
                    continue
                }

                i++

                var anyMatch = false
                val variableNameBuilder = StringBuilder()
                while (i < array.size && array[i].toString().matches(VariableSetStatement.VARIABLE_LETTER_REGEX))
                {
                    anyMatch = true
                    variableNameBuilder.append(array[i++])
                }
                i--

                if(!anyMatch)
                {
                    out.append(array[i++])
                    continue
                }

                out.append((context.getVariable(variableNameBuilder.toString())).toString())
            }
            else
            {
                out.append(array[i])
            }
            i++
        }

        return out.toString()
    }
}