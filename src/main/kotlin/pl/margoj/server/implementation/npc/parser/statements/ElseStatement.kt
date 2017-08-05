package pl.margoj.server.implementation.npc.parser.statements

import pl.margoj.server.implementation.npc.parser.CodeLine
import pl.margoj.server.implementation.npc.parser.CodeParser
import pl.margoj.server.implementation.npc.parser.blocks.CodeBlock
import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext

class ElseStatement : IfStatement()
{
    private lateinit var previous: IfStatement

    override fun init(function: String, parser: CodeParser, line: CodeLine)
    {
        this.previous = (parser.currentBlock as? CodeBlock)?.lastStatement as? IfStatement ?: parser.throwError("Brak pasującego jeżeli/lub")
        this.previous.nextIf = this

        parser.openBlock(codeBlock)
    }

    override fun execute(context: ScriptContext)
    {
    }

    override fun evaluateExpression(context: ScriptContext): Boolean
    {
        return true
    }
}