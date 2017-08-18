package pl.margoj.server.implementation

import org.apache.logging.log4j.Logger
import pl.margoj.mrf.MargoResource
import pl.margoj.server.api.MargoJConfig
import pl.margoj.server.api.Server
import pl.margoj.server.api.events.ServerReadyEvent
import pl.margoj.server.api.inventory.Item
import pl.margoj.server.api.inventory.ItemStack
import pl.margoj.server.implementation.auth.Authenticator
import pl.margoj.server.implementation.chat.ChatManagerImpl
import pl.margoj.server.implementation.commands.CommandsManagerImpl
import pl.margoj.server.implementation.commands.console.ConsoleCommandSenderImpl
import pl.margoj.server.implementation.commands.defaults.DefaultCommands
import pl.margoj.server.implementation.database.DatabaseManager
import pl.margoj.server.implementation.database.DatabaseSaveThread
import pl.margoj.server.implementation.entity.EntityManagerImpl
import pl.margoj.server.implementation.event.EventManagerImpl
import pl.margoj.server.implementation.item.ItemImpl
import pl.margoj.server.implementation.item.ItemManager
import pl.margoj.server.implementation.map.TownImpl
import pl.margoj.server.implementation.network.handlers.*
import pl.margoj.server.implementation.network.http.HttpServer
import pl.margoj.server.implementation.network.protocol.NetworkManager
import pl.margoj.server.implementation.npc.parser.NpcScriptParser
import pl.margoj.server.implementation.player.PlayerImpl
import pl.margoj.server.implementation.plugin.PluginManagerImpl
import pl.margoj.server.implementation.resources.ResourceBundleManager
import pl.margoj.server.implementation.resources.ResourceLoader
import pl.margoj.server.implementation.sync.SchedulerImpl
import pl.margoj.server.implementation.sync.TickerImpl
import pl.margoj.server.implementation.threads.PlayerKeepAlive
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class ServerImpl(override val config: MargoJConfig, override val logger: Logger) : Server
{
    private var towns_ = hashMapOf<String, TownImpl>()
    private var items_ = hashMapOf<String, ItemImpl>()

    var debugModeEnabled: Boolean = false
    var useJLine: Boolean = true

    override val version get() = VERSION
    override val name: String get() = this.config.serverConfig!!.name
    override val players: Collection<PlayerImpl> get() = this.entityManager.players
    override val towns: Collection<TownImpl> get() = this.towns_.values
    override val items: Collection<ItemImpl> get() = this.items_.values

    override var running = false
    override val ticker = TickerImpl(this, Thread.currentThread(), config.engineConfig.targetTps)
    override val scheduler = SchedulerImpl(this)
    override val pluginManager = PluginManagerImpl(this)
    override val eventManager = EventManagerImpl(this)
    override val commandsManager = CommandsManagerImpl(this)
    override val consoleCommandSender = ConsoleCommandSenderImpl(this)
    override val entityManager = EntityManagerImpl(this)
    override val chatManager = ChatManagerImpl(this)

    val authenticator = Authenticator()
    val networkManager = NetworkManager(this)
    val itemManager = ItemManager(this)
    val databaseManager = DatabaseManager(this)
    val npcScriptParser = NpcScriptParser()

    lateinit var httpServer: HttpServer
        private set

    lateinit var resourceBundleManager: ResourceBundleManager
        private set

    fun start()
    {
        if (this.running)
        {
            throw IllegalStateException("Already running")
        }

        logger.info("Uruchamiam serwer MargoJ v$version...")
        this.running = true

        logger.info("Ładuje pluginy...")
        val pluginsDirectory = File("plugins")
        if (!pluginsDirectory.exists())
        {
            pluginsDirectory.mkdir()
        }
        this.pluginManager.loadAll(pluginsDirectory)

        val webFolder = File("web")

        try
        {
            val installer = GameInstaller(webFolder, logger)

            if (!installer.isUpdated())
            {
                if (!installer.canUpdate())
                {
                    for (i in 0..10)
                    {
                        logger.warn("Nowa wersja clienta jest juz dostepna!")
                    }
                    logger.warn("Aby zezwoic na aktualizacje usun plik '${GameInstaller.VERSION_FILE}' znajdujacy sie w folderze 'web'")
                }
                else
                {
                    logger.info("Przystepuje do aktualizacji clienta gry")
                    installer.update()
                }
            }
        }
        catch (e: IOException)
        {
            logger.warn("Wystapil blad podczas aktualizacji clienta", e)

            if (!webFolder.exists())
            {
                logger.error("Brak plikow clienta, wylaczam serwer...")
                return
            }
        }

        // database
        this.databaseManager.start()
        DatabaseSaveThread(this.databaseManager, this.config.mySQLConfig.saveIntervalSeconds).start()
        this.itemManager.initCounter()

        // synchronization
        this.scheduler.start()

        // network
        val httpConfig = this.config.httpConfig

        httpServer = HttpServer(this.logger, httpConfig.host, httpConfig.port)

        val engineHandler = EngineHandler(this)
        httpServer.registerHandler(engineHandler)
        httpServer.registerHandler(TownHandler(this))
        httpServer.registerHandler(ItemsHandler(this))
        httpServer.registerHandler(ResourceHandler(webFolder.absoluteFile))
        httpServer.registerHandler(TemporaryLoginHandler(this))

        if (this.debugModeEnabled)
        {
            this.httpServer.registerHandler(DebugHandler(this))
        }

        // load resources
        // TODO
        this.resourceBundleManager = ResourceBundleManager(this, File("resources"), File("mounts"))

        logger.info("Dostępne zestawy zasobów: " + this.resourceBundleManager.resources)

        this.resourceBundleManager.loadBundle("testowe_zasoby")

        var resourceLoader: ResourceLoader? = ResourceLoader(this.resourceBundleManager, File("cache/${this.resourceBundleManager.currentBundleName}"))
        resourceLoader!!

        for (view in this.resourceBundleManager.currentBundle!!.resources)
        {
            when (view.category)
            {
                MargoResource.Category.MAPS ->
                {
                    this.towns_.put(view.id, resourceLoader.loadMap(view.id)!!)
                }
                MargoResource.Category.ITEMS ->
                {
                    this.items_.put(view.id, resourceLoader.loadItem(view.id)!!)
                }
                MargoResource.Category.TILESETS ->
                {
                    logger.trace("Załadowano tileset: ${view.fileName}")
                }
                MargoResource.Category.NPC_SCRIPTS ->
                {
                    npcScriptParser.parse(view.id, resourceLoader.loadScript(view.id)!!.content)
                }
                MargoResource.Category.GRAPHIC ->
                {
                    resourceLoader.loadGraphic(view.id)
                }
            }
        }

        val graphicsCacheDirectory = resourceLoader.graphicsCacheDirectory
        resourceLoader = null

        // late network handlers
        httpServer.registerHandler(GraphicsHandler(this, graphicsCacheDirectory))

        // load npcs
        this.towns.forEach {
            it.updateNpcs()
        }

        // preload data
        this.databaseManager.withSkippedCheck {
            this.databaseManager.preloadData()
        }

        // tickables
        this.ticker.registerTickable(PlayerKeepAlive(this, config.engineConfig.keepAliveSeconds))

        // register core commands
        DefaultCommands.registerDefaults(this.commandsManager)

        // init ticker
        this.ticker.init()

        // start http server
        httpServer.start()

        // start console reader thread
        val consoleReader = ConsoleReaderThread(this, this.useJLine)
        consoleReader.isDaemon = true
        consoleReader.start()

        // call ready event
        this.eventManager.call(ServerReadyEvent(this))

        // main game loop
        while (this.running)
        {
            try
            {
                this.ticker.tick()
            }
            catch(e: Exception)
            {
                e.printStackTrace()
            }
        }

        logger.warn("Serwer zostanie wyłączony w ciągu 10 sekund!")
        this.httpServer.unregisterHandler(engineHandler)
        this.httpServer.registerHandler(ShutdownHandler(this))
        this.ticker.stop()
        this.ticker.mainThread = null
        this.databaseManager.stop()

        Thread.sleep(TimeUnit.SECONDS.toMillis(10L))

        this.httpServer.shutdown()
    }

    override fun shutdown()
    {
        this.running = false
    }

    override fun getItemById(id: String): Item?
    {
        return this.items_[id]
    }

    override fun getTownById(id: String): TownImpl?
    {
        return this.towns_[id]
    }

    override fun newItemStack(item: Item): ItemStack
    {
        return this.itemManager.newItemStack(item as ItemImpl)
    }

    companion object
    {
        const val VERSION = "1.0"
    }
}