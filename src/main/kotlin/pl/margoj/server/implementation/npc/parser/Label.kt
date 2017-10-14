package pl.margoj.server.implementation.npc.parser

interface Label
{
    val name: String

    val type: Int
}

data class CodeLabel(override val name: String, override val type: Int = 2) : Label

class SystemLabel private constructor(override val name: String, override val type: Int) : Label
{
    companion object
    {
        private val ALL = ArrayList<SystemLabel>()

        val END = SystemLabel("zakończ", 6)
        val END_NORMAL_TYPE = SystemLabel("koniec", 2)

        fun find(name: String): SystemLabel?
        {
            return ALL.firstOrNull { it.name == name }
        }
    }

    init
    {
        ALL.add(this)
    }

    override fun toString(): String
    {
        return "SystemLabel(name=$name)"
    }
}