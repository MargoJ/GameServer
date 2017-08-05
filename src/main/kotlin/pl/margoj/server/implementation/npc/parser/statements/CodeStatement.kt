package pl.margoj.server.implementation.npc.parser.statements

import pl.margoj.server.implementation.npc.parser.CodeLine
import pl.margoj.server.implementation.npc.parser.CodeParser
import pl.margoj.server.implementation.npc.parser.parsed.ScriptContext

abstract class CodeStatement
{
    abstract fun init(function: String, parser: CodeParser, line: CodeLine)

    abstract fun execute(context: ScriptContext)

    companion object
    {
        val types = HashMap<String, () -> CodeStatement>()

        init
        {
            types.put("jeżeli", ::IfStatement)
            types.put("lub", ::ElseIfStatement)
            types.put("przeciwnie", ::ElseStatement)

            types.put("dopóki", ::WhileStatement)
            types.put("każdy", ::EveryStatement)

            types.put("ustaw", ::VariableSetStatement)

            types.put("nazwa", ::DelegatedFunctionStatement)
            types.put("dialog", ::DelegatedFunctionStatement)
            types.put("opcja", ::DelegatedFunctionStatement)
            types.put("poziom", ::DelegatedFunctionStatement)
            types.put("level", ::DelegatedFunctionStatement)
            types.put("grafika", ::DelegatedFunctionStatement)

            types.put(MathStatement.ADD, ::MathStatement)
            types.put(MathStatement.SUBTRACT, ::MathStatement)
            types.put(MathStatement.MULTIPLY, ::MathStatement)
            types.put(MathStatement.DIVIDE, ::MathStatement)
        }
    }
}