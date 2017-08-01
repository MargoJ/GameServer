package pl.margoj.server.implementation.npc.parser

import java.lang.StringBuilder
import java.util.LinkedList

abstract class AbstractBlock
{
    lateinit var parser: CodeParser
    lateinit var line: CodeLine
    var indentLevel: Int = 0
    var children = LinkedList<AbstractBlock>()

    abstract fun parse(codeParser: CodeParser, currentLine: CodeLine)

    open fun start()
    {
    }

    open fun end()
    {
    }

    open val name: String = ""

    override fun toString(): String
    {
        val out = StringBuilder()
        for(i in 1..this.indentLevel)
        {
            out.append("\t")
        }

        out.append(this.javaClass.simpleName)

        if(this.name.isNotEmpty())
        {
            out.append(" - ").append(this.name)
        }

        for (child in this.children)
        {
            out.append("\n").append(child)
        }

        return out.toString()
    }
}