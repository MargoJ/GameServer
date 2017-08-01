package pl.margoj.server.implementation.npc.parser

class CodeLine(val realIndex: Int, line: String)
{
    var line: String = line
        set(value)
        {
            field = value
            this.array = this.line.toCharArray()
            this.currentIndex = 0
        }

    var array: CharArray = this.line.toCharArray()

    var currentIndex = 0
    val last get() = array.size - 1
    var indent: Int = 0

    fun calculateIndent()
    {
        var currentIndent = 0

        while (current == TAB)
        {
            currentIndent++
            this.next()
        }

        indent = currentIndent
    }

    fun next()
    {
        currentIndex++
    }

    val current: Char get() = this.array[currentIndex]
    val finished get() = this.currentIndex > this.last

    fun rest(): String
    {
        var out = ""
        for (i in currentIndex..last)
        {
            out += this.array[i]
        }
        return out
    }

    fun skipSpaces()
    {
        while (!finished && current == ' ')
        {
            this.next()
        }
    }

    fun readUntilSpace(): String
    {
        return this.readUntil(' ')
    }

    fun readUntil(character: Char): String
    {
        val word = StringBuilder()

        while (!this.finished)
        {
            if (current == character)
            {
                break
            }

            word.append(current)

            this.next()
        }

        return word.toString()
    }

    fun readIfMatches(regex: Regex): String
    {
        val word = StringBuilder()

        while (!this.finished)
        {
            if (!regex.matches(current.toString()))
            {
                break
            }

            word.append(current)

            this.next()
        }

        return word.toString()
    }
}