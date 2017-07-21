package pl.margoj.server.implementation

import pl.margoj.server.api.MargoJConfig

data class MargoJConfigImpl(
        var server: MargoJConfigImpl.ServerConfigImpl = MargoJConfigImpl.ServerConfigImpl(),
        var http: MargoJConfigImpl.HttpConfigImpl = MargoJConfigImpl.HttpConfigImpl(),
        var engine: MargoJConfigImpl.EngineConfigImpl = MargoJConfigImpl.EngineConfigImpl()
) : MargoJConfig
{
    data class ServerConfigImpl(override var name: String = "") : MargoJConfig.ServerConfig

    override val serverConfig: MargoJConfig.ServerConfig
        get() = this.server

    data class HttpConfigImpl(override var host: String = "", override var port: Int = 0) : MargoJConfig.HttpConfig

    override val httpConfig: MargoJConfig.HttpConfig
        get() = this.http

    data class EngineConfigImpl(var tps: Int = 0, var keepalive: Int = 0) : MargoJConfig.EngineConfig
    {
        override val keepAliveSeconds: Int
            get() = this.keepalive

        override val targetTps: Int
            get() = this.tps
    }

    override val engineConfig: MargoJConfig.EngineConfig
        get() = this.engine
}