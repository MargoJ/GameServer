package pl.margoj.server.implementation.auth

import pl.margoj.server.api.player.PlayerRank

data class AuthSession(
        val gameToken: String,
        val accountId: Long,
        val charId: Long,
        val sessionId: Long,
        val charName: String,
        val charProfession: String,
        val charGender: String,
        val rank: PlayerRank
)
{
    var invalidated = false
        set(value)
        {
            if(field && !value)
            {
                throw IllegalStateException("session is invalidated")
            }

            field = value
        }
}