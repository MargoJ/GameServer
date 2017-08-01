package pl.margoj.server.implementation.npc.parser.constants

import pl.margoj.server.implementation.npc.parser.SystemLabel
import pl.margoj.server.implementation.npc.parser.CodeLine
import pl.margoj.server.implementation.npc.parser.CodeParser
import pl.margoj.server.implementation.npc.parser.ConstantParser

class SystemLabelConstantParser : ConstantParser<SystemLabel>()
{
    override fun tryParse(parser: CodeParser, line: CodeLine): SystemLabel?
    {
        if (line.current != '%')
        {
            return null
        }

        val label = line.readUntilSpace()
        if(label.length < 2)
        {
            return null
        }

        return SystemLabel(label.substring(1))
    }
}