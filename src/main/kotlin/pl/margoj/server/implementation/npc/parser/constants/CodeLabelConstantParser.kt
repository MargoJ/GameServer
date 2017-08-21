package pl.margoj.server.implementation.npc.parser.constants

import pl.margoj.server.implementation.npc.parser.CodeLabel
import pl.margoj.server.implementation.npc.parser.CodeLine
import pl.margoj.server.implementation.npc.parser.CodeParser
import pl.margoj.server.implementation.npc.parser.ConstantParser

class CodeLabelConstantParser : ConstantParser<CodeLabel>()
{
    override fun tryParse(parser: CodeParser, line: CodeLine): CodeLabel?
    {
        if (line.current != '@')
        {
            return null
        }

        val label = line.readUntilSpace()
        if (label.length < 2)
        {
            return null
        }

        return CodeLabel(label.substring(1))
    }
}