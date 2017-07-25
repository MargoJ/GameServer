package pl.margoj.server.implementation.database

import org.apache.commons.io.IOUtils
import java.nio.charset.StandardCharsets
import java.sql.Statement

internal class DatabaseInitializer(var databaseManager: DatabaseManager)
{
    fun init()
    {
        this.databaseManager.withConnectionUnsafe { connection ->
            // prerequisites
            val logger = databaseManager.server.logger
            var currentVersion_: Int? = null
            val statement = connection.createStatement()
            val selectVersionQuery = connection.prepareStatement("SELECT `version` FROM `${TableNames.SCHEMA_VERSION}`")

            try
            {

                // check version
                connection.metaData.getTables(null, null, TableNames.SCHEMA_VERSION, null).use { result ->
                    if (result.next())
                    {
                        selectVersionQuery.executeQuery().use { queryResult ->
                            if (queryResult.next())
                            {
                                currentVersion_ = queryResult.getInt("version")
                                logger.trace("Znaleziono wersje w `${TableNames.SCHEMA_VERSION}`, currentVersion = $currentVersion_")
                            }
                            else
                            {
                                logger.trace("Brak wpisow w `${TableNames.SCHEMA_VERSION}`, currentVersion = 0")
                                currentVersion_ = 0
                            }
                        }
                    }
                    else
                    {
                        logger.trace("Brak tabeli `${TableNames.SCHEMA_VERSION}`, currentVersion = 0")
                        currentVersion_ = 0
                    }
                }

                // load scripts
                var currentVersion = currentVersion_!!

                val classLoader = this.javaClass.classLoader

                while (true)
                {
                    currentVersion++

                    val sqlResource = classLoader.getResourceAsStream("sql/$currentVersion.sql")
                    if (sqlResource == null)
                    {
                        currentVersion--
                        logger.info("Zakonczono aktualizacje bazy danych, aktualna wersja: $currentVersion")
                        break
                    }

                    val sqlScript = IOUtils.toString(sqlResource, StandardCharsets.UTF_8)
                    logger.info("Aktualizuje bazy danych z wersji ${currentVersion - 1} do $currentVersion")
                    runSqlFile(statement, sqlScript)

                    selectVersionQuery.executeQuery().use {
                        val afterUpdate: Int
                        if (it.next())
                        {
                            afterUpdate = it.getInt("version")
                        }
                        else
                        {
                            afterUpdate = 0
                        }

                        if (afterUpdate != currentVersion)
                        {
                            throw IllegalStateException("Aktualizacja bazy nie powiodla sie: spodziewana wersje: $currentVersion, aktualna wersja: $afterUpdate")
                        }
                    }
                }
            }
            finally
            {
                statement.close()
                selectVersionQuery.close()
            }
        }
    }

    private fun runSqlFile(statement: Statement, sqlScript: String)
    {
        for (sql in sqlScript.split(";"))
        {
            if(sql.trim().isEmpty())
            {
                continue
            }

            databaseManager.server.logger.trace("SQL: \n$sql")

            statement.execute(sql)
        }
    }
}