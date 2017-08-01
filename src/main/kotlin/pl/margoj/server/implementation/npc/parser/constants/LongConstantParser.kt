package pl.margoj.server.implementation.npc.parser.constants

import pl.margoj.server.api.utils.Parse
import pl.margoj.server.implementation.npc.parser.CodeLine
import pl.margoj.server.implementation.npc.parser.CodeParser
import pl.margoj.server.implementation.npc.parser.ConstantParser

class LongConstantParser : ConstantParser<Long>()
{
    override fun tryParse(parser: CodeParser, line: CodeLine): Long?
    {
        return Parse.parseLong(line.readUntilSpace())
    }
}