package pl.margoj.server.implementation.npc.parser.statements

import pl.margoj.server.implementation.npc.parser.CodeLine
import pl.margoj.server.implementation.npc.parser.CodeParser
import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext

abstract class CodeStatement
{
    abstract fun execute(context: ScriptContext)

    companion object
    {
        val types = HashMap<String, (String, CodeParser, CodeLine) -> CodeStatement>()

        init
        {
            types.put("ustaw", ::VariableSetStatement)
            types.put("jeżeli", ::IfStatement)
            types.put("dopóki", ::WhileStatement)
            types.put("każdy", ::EveryStatement)

            types.put("nazwa", ::DelegatedFunctionStatement)
            types.put("dialog", ::DelegatedFunctionStatement)
            types.put("opcja", ::DelegatedFunctionStatement)

            types.put(MathStatement.ADD, ::MathStatement)
            types.put(MathStatement.SUBTRACT, ::MathStatement)
            types.put(MathStatement.MULTIPLY, ::MathStatement)
            types.put(MathStatement.DIVIDE, ::MathStatement)
        }
    }
}