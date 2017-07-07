package pl.margoj.server.implementation

import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.FileInputStream
import java.net.Socket
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Scanner

class GameInstaller(val targetDirectory: File, val logger: Logger)
{
    companion object
    {
        val BASE_URL = "https://margoj.pl/installer/"
        val VERSION_FILE = "latest.txt"
        val DOWNLOADS_FILE = "downloads.txt"
        val REPLACEMENTS_FILE = "replacements.txt"
        val SEPARATOR = "    "
    }

    fun canUpdate(): Boolean
    {
        return !File(this.targetDirectory, VERSION_FILE).exists()
    }

    fun isUpdated(): Boolean
    {
        val localVersionFile = File(this.targetDirectory, VERSION_FILE)
        if (!localVersionFile.exists())
        {
            return false
        }

        var localVersion: String? = null

        Scanner(FileInputStream(localVersionFile)).use {
            localVersion = it.nextLine()
        }

        Scanner(URL(BASE_URL + VERSION_FILE).openStream()).use {
            return localVersion!! == it.nextLine()
        }
    }

    fun update()
    {
        FileUtils.deleteDirectory(this.targetDirectory)
        this.targetDirectory.mkdir()

        Scanner(URL(BASE_URL + DOWNLOADS_FILE).openStream()).use {
            while (it.hasNextLine())
            {
                val array = StringUtils.splitByWholeSeparator(it.nextLine(), SEPARATOR)
                val url = URL(array[0])
                val target = File(this.targetDirectory, array[1])

                logger.info("Pobieranie $url do $target")

                target.parentFile.mkdirs()
                FileUtils.copyURLToFile(url, target)
            }
        }

        Scanner(URL(BASE_URL + REPLACEMENTS_FILE).openStream()).use {
            val changes = HashMap<String, MutableList<Pair<String, String>>>()

            while (it.hasNextLine())
            {
                val array = StringUtils.splitByWholeSeparator(it.nextLine(), SEPARATOR)

                changes.computeIfAbsent(array[0], { ArrayList() }).add(Pair(array[1], if(array.size >= 3) array[2] else ""))
            }

            for (change in changes)
            {
                logger.info("Zamieniam w pliku: ${change.key}")

                val file = File(this.targetDirectory, change.key)
                var string = FileUtils.readFileToString(file, StandardCharsets.UTF_8)

                for (pair in change.value)
                {
                    logger.info("Zamieniam ${pair.first} na ${pair.second}")
                    string = StringUtils.replace(string, pair.first, pair.second)
                }

                FileUtils.writeStringToFile(file, string, StandardCharsets.UTF_8)
            }
        }
    }
}
