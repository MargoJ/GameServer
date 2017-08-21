package pl.margoj.server.implementation.npc.parser

import pl.margoj.server.implementation.npc.parser.constants.*

abstract class ConstantParser<out T>
{
    companion object
    {
        val ALL = ArrayList<ConstantParser<*>>()

        init
        {
            ALL.add(ExpressionConstantParser())
            ALL.add(StringConstantParser())
            ALL.add(BooleanConstantParser())
            ALL.add(CodeLabelConstantParser())
            ALL.add(SystemLabelConstantParser())
            ALL.add(VariableConstantParser())
            ALL.add(LongConstantParser())
        }
    }

    abstract fun tryParse(parser: CodeParser, line: CodeLine): T?
}