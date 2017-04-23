package pl.margoj.server.implementation

import org.apache.logging.log4j.Logger
import pl.margoj.mrf.MargoResource
import pl.margoj.server.api.MargoJConfig
import pl.margoj.server.api.Server
import pl.margoj.server.api.map.Town
import pl.margoj.server.implementation.auth.Authenticator
import pl.margoj.server.implementation.chat.ChatManagerImpl
import pl.margoj.server.implementation.commands.CommandsManagerImpl
import pl.margoj.server.implementation.entity.EntityManagerImpl
import pl.margoj.server.implementation.map.TownImpl
import pl.margoj.server.implementation.network.handlers.DebugHandler
import pl.margoj.server.implementation.network.handlers.EngineHandler
import pl.margoj.server.implementation.network.handlers.ResourceHandler
import pl.margoj.server.implementation.network.handlers.TownHandler
import pl.margoj.server.implementation.network.http.HttpServer
import pl.margoj.server.implementation.network.protocol.NetworkManager
import pl.margoj.server.implementation.player.PlayerImpl
import pl.margoj.server.implementation.resources.ResourceBundleManager
import pl.margoj.server.implementation.resources.ResourceLoader
import pl.margoj.server.implementation.sync.SchedulerImpl
import pl.margoj.server.implementation.sync.TickerImpl
import pl.margoj.server.implementation.threads.PlayerKeepAlive
import java.io.File

class ServerImpl(override val config: MargoJConfig, override val logger: Logger) : Server
{
    private var towns_ = hashMapOf<String, TownImpl>()

    var debugModeEnabled: Boolean = false
    var useJLine: Boolean = true

    override val version get() = VERSION
    override val players: Collection<PlayerImpl> get() = this.entityManager.players
    override val towns: Collection<Town> get() = this.towns_.values

    override var running = false
    override val ticker = TickerImpl(this, Thread.currentThread())
    override val scheduler = SchedulerImpl()
    override val commandsManager = CommandsManagerImpl(this)
    override val entityManager = EntityManagerImpl(this)
    override val chatManager = ChatManagerImpl(this)

    val authenticator = Authenticator()
    val networkManager = NetworkManager(this)

    lateinit var httpServer: HttpServer
        private set

    lateinit var resourceBundleManager: ResourceBundleManager
        private set

    lateinit var resourceLoader: ResourceLoader
        private set


    fun start()
    {
        if (this.running)
        {
            throw IllegalStateException("Already running")
        }

        logger.info("Uruchamiam serwer MargoJ v$version...")
        this.running = true

        // synchronization
        ticker.targetTps = config.engineConfig.targetTps
        ticker.registerTickable(this.scheduler)

        // network
        val httpConfig = this.config.httpConfig

        httpServer = HttpServer(this.logger, httpConfig.host, httpConfig.port)

        httpServer.registerHandler(EngineHandler(this))
        httpServer.registerHandler(TownHandler(this))
        httpServer.registerHandler(ResourceHandler("static", ServerImpl::class.java.classLoader))

        if (this.debugModeEnabled)
        {
            this.httpServer.registerHandler(DebugHandler(this))
        }

        // load resources
        // TODO
        this.resourceBundleManager = ResourceBundleManager(this, File("resources"), File("mounts"))
        this.resourceLoader = ResourceLoader(this.resourceBundleManager)

        logger.info("Dostępne zestawy zasobów: " + this.resourceBundleManager.resources)

        this.resourceBundleManager.loadBundle("testowe_zasoby")

        for (view in this.resourceBundleManager.currentBundle!!.resources)
        {
            when (view.category)
            {
                MargoResource.Category.MAPS ->
                {
                    this.towns_.put(view.id, this.resourceLoader.loadMap(view.id)!!)
                }
                MargoResource.Category.ITEMS ->
                {
                }
            }
        }

        // tickables
        this.ticker.registerTickable(PlayerKeepAlive(this, config.engineConfig.keepAliveSeconds))

        // init ticker
        this.ticker.init()

        // start http server
        httpServer.start()

        // main game loop
        while (this.running)
        {
            this.ticker.tick()
        }

        // shutdown task
        this.httpServer.shutdown()
    }

    override fun shutdown()
    {
    }

    override fun getTownById(id: String): TownImpl?
    {
        return this.towns_[id]
    }

    companion object
    {
        const val VERSION = "1.0"
    }
}