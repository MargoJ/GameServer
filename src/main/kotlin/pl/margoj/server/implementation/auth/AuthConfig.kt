package pl.margoj.server.implementation.auth

data class AuthConfig
(
        var authserver: String = "",

        var serverid: String = "",

        var secret: String = "",

        var loginpage: String = "",

        var server: String = ""
)