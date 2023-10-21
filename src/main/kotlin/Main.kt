import api.Server
import command.Command.cmd
import org.slf4j.LoggerFactory
import utils.DataUtils
import java.util.*

/**
 * @Description
 * @Author littleArray
 * @Date 2023/10/20
 */
fun main() {
    val logger = LoggerFactory.getLogger("main")
    DataUtils.createDir()
    ServerInstance.load()
    ServerInstance.INSTANCE.forEach {
        it.updatePlayerList()
    }
    val scanner = Scanner(System.`in`)
    while (true) {
        print("> ")
        cmd = scanner.nextLine()
    }
}