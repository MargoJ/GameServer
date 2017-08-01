package pl.margoj.server.implementation.npc.parser

import org.apache.commons.lang3.StringUtils
import pl.margoj.server.implementation.npc.parser.blocks.GlobalBlock
import pl.margoj.server.implementation.npc.parser.constants.StringConstantParser
import pl.margoj.server.implementation.npc.parser.statements.CodeStatement
import java.util.Deque
import java.util.LinkedList

const val TAB = '\t'

class CodeParser(code: String)
{
    val lines = StringUtils.splitByWholeSeparator(code, "\n")
            .withIndex()
            .map { this.stripComments(CodeLine(it.index, it.value)) }
            .filter { !it.line.trim().isEmpty() }
            .map { it.calculateIndent(); it }

    var currentBlockStack: Deque<AbstractBlock> = LinkedList()

    init
    {
        currentBlockStack.push(GlobalBlock())
    }

    var currentLineIndex = 0
        private set

    var currentLine: CodeLine? = null
        private set

    fun hasMore(): Boolean
    {
        return currentLineIndex < this.lines.size
    }

    fun parseLine()
    {
        currentLine = lines[currentLineIndex]

        while (!currentBlockStack.isEmpty())
        {
            if (currentLine!!.indent < currentBlockStack.peek().indentLevel)
            {
                currentBlockStack.pop().end()
            }
            else
            {
                break
            }
        }

        this.currentBlockStack.peek().parse(this, currentLine!!)

        currentLineIndex++
    }

    fun finish(): AbstractBlock?
    {
        var last: AbstractBlock? = null

        while (!currentBlockStack.isEmpty())
        {
            last = currentBlockStack.pop()
            last?.end()
        }

        return last
    }

    fun parseLiteral(codeLine: CodeLine = this.currentLine!!): Any?
    {
        val before = codeLine.currentIndex

        for (parser in ConstantParser.ALL)
        {
            codeLine.currentIndex = before
            val tryParse = parser.tryParse(this, codeLine)

            if (tryParse != null)
            {
                return tryParse
            }
        }

        return null
    }

    fun parseStatement(): CodeStatement
    {
        val name = this.currentLine!!.readUntilSpace()
        val type = CodeStatement.types[name] ?: this.throwError("nieznana instrukcja $name")
        return type(name, this, this.currentLine!!)
    }

    fun openBlock(block: AbstractBlock)
    {
        this.currentBlockStack.peek().children.add(block)
        block.parser = this
        block.line = this.currentLine!!
        block.indentLevel = this.currentLine!!.indent + 1
        currentBlockStack.push(block)
        block.start()
    }

    fun throwError(message: String): Nothing
    {
        val error = StringBuilder("\n\tBłąd parsowania skryptu: ").append(message).append("\n")
        error.append("\t\t").append("linia=").append(this.currentLine!!.realIndex + 1).append("\n")
        error.append("\t\t").append("kolumna=").append(this.currentLine!!.currentIndex).append("\n")
        error.append("\n")
        error.append("\t\t${this.currentLine!!.line}").append("\n")
        error.append("\t\t")
        for (i in 1..this.currentLine!!.currentIndex)
        {
            error.append(" ")
        }
        error.append("^").append("\n")

        throw ParsingError(error.toString())
    }

    fun stripComments(line: CodeLine): CodeLine
    {
        val current = StringBuilder()

        while (!line.finished)
        {
            if (line.currentIndex == line.last)
            {
                current.append(line.current)
                line.next()
                break
            }

            val startingIndex = line.currentIndex
            StringConstantParser.INSTANCE.tryParse(this, line)
            val endingIndex = line.currentIndex

            if(startingIndex != endingIndex)
            {
                current.append(line.line.substring(startingIndex, endingIndex))
                continue
            }

            if (line.finished)
            {
                break
            }

            if (line.current == '-' && line.array[line.currentIndex + 1] == '-')
            {
                line.next()
                break
            }

            current.append(line.current)
            line.next()
        }

        line.line = current.toString()
        return line
    }
}

class ParsingError(message: String) : RuntimeException(message)
