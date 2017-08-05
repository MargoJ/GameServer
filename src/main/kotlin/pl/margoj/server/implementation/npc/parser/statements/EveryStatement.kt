package pl.margoj.server.implementation.npc.parser.statements

import pl.margoj.server.implementation.npc.parser.CodeLine
import pl.margoj.server.implementation.npc.parser.CodeParser
import pl.margoj.server.implementation.npc.parser.blocks.CodeBlock
import pl.margoj.server.implementation.npc.parser.constants.VariableConstant
import pl.margoj.server.implementation.npc.parser.parsed.NpcCodeBlock
import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext

class EveryStatement : CodeStatement()
{
    private companion object
    {
        var everyLabelCounter = 1
    }

    private lateinit var variableIterated: VariableConstant
    private lateinit var variableList: VariableConstant

    private val codeBlock: CodeBlock = CodeBlock("EVERY_LABEL_${everyLabelCounter++}")
    private val npcCodeBlock: NpcCodeBlock by lazy { NpcCodeBlock(this.codeBlock) }

    override fun init(function: String, parser: CodeParser, line: CodeLine)
    {
        line.skipSpaces()
        this.variableIterated = parser.parseLiteral() as VariableConstant
        line.skipSpaces()

        if (line.readUntilSpace() != "w")
        {
            parser.throwError("niepoprawna skladnia! poprawna: każdy !element w !lista")
        }

        line.skipSpaces()
        this.variableList = parser.parseLiteral() as VariableConstant

        parser.openBlock(codeBlock)
    }

    @Suppress("UNCHECKED_CAST")
    override fun execute(context: ScriptContext)
    {
        val list = context.getVariable(this.variableList.name) as List<Any>

        for (any in list)
        {
            context.setVariable(this.variableIterated.name, any)
            this.npcCodeBlock.execute(context)
        }
    }
}