package pl.margoj.server.implementation.network.protocol

data class IncomingPacket(
        val type: String,
        val queryParams: Map<String, String>,
        val body: Map<String, String>
)