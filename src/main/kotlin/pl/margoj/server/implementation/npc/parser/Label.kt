package pl.margoj.server.implementation.npc.parser

interface Label
{
    val name: String
}

data class CodeLabel(override val name: String) : Label
data class SystemLabel(override val name: String) : Label