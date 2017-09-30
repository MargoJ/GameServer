package pl.margoj.server.implementation.auth

data class AuthSession(
        val gameToken: String,
        val accountId: Long,
        val charId: Long,
        val sessionId: Long,
        val charName: String,
        val charProfession: String,
        val charGender: String
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