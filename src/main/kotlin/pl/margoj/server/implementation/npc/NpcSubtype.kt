package pl.margoj.server.implementation.npc

enum class NpcSubtype(val margoId: Int)
{
    NORMAL(0),

    ELITE1(10),

    ELITE2(20),

    ELITE3(30),

    HERO(80),

    TITAN(100)
}