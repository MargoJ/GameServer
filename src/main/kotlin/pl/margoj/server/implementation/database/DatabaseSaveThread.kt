package pl.margoj.server.implementation.database

import java.util.concurrent.TimeUnit

class DatabaseSaveThread(val databaseManager: DatabaseManager, val seconds: Int) : Thread()
{
    init
    {
        this.isDaemon = true
    }

    override fun run()
    {
        Thread.sleep(TimeUnit.SECONDS.toMillis(this.seconds.toLong()))

        databaseManager.server.logger.info("Zapisuje dane do bazy danych...")
        this.databaseManager.saveAll()
    }
}