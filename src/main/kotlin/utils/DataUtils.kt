package utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

/**
 * @Description
 * @Author littleArray
 * @Date 2023/10/21
 */
object DataUtils {
    private val currentDir: String = File(".").absoluteFile.parent
    private val p: String = System.getProperty("file.separator")
    private val loger: Logger = LoggerFactory.getLogger(this.javaClass)
    fun createDir() {
        File(currentDir + p + "data").mkdirs()
        File(currentDir + p + "setting").mkdirs()
    }

    fun save(fileName: String, content: String) {
        val file = File(currentDir + p + "setting" + p + fileName + ".json")
        file.createNewFile()
        file.writeText(content)
    }

    fun load(fileName: String): String {
        val file = File(currentDir + p + "setting" + p + fileName + ".json")
        return file.readText()
    }
}