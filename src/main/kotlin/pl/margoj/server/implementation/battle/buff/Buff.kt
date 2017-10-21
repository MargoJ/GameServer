package pl.margoj.server.implementation.battle.buff

abstract class Buff
{
    open val margoId: Int = 0

    var activeUntil = 0
}