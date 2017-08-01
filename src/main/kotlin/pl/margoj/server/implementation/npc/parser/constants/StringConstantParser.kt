package pl.margoj.server.implementation.npc.parser.constants

import pl.margoj.server.implementation.npc.parser.CodeLine
import pl.margoj.server.implementation.npc.parser.CodeParser
import pl.margoj.server.implementation.npc.parser.ConstantParser

class StringConstantParser : ConstantParser<String>()
{
    companion object
    {
        val INSTANCE = StringConstantParser()
    }

    override fun tryParse(parser: CodeParser, line: CodeLine): String?
    {
        if (line.current != '"')
        {
            return null
        }

        line.next()

        val string = StringBuilder()
        while (!line.finished)
        {
            if (line.current == '\\')
            {
                if (line.finished)
                {
                    break
                }

                line.next()

                if (line.current == '!')
                {
                    string.append("\\") // needed in value fetching
                }

                string.append(line.current)
                line.next()
            }
            else if (line.current == '"')
            {
                line.next()
                return string.toString()
            }
            else
            {
                string.append(line.current)
                line.next()
            }
        }

        parser.throwError("String musi być zakończony znakiem \"")
    }
}