import command.Command.cmd
import instance.ServerInstance
import org.slf4j.LoggerFactory
import utils.DataUtils
import java.util.*

/**
 * @Description
 * @Author littleArray
 * @Date 2023/10/20
 */
fun main() {
    val logger = LoggerFactory.getLogger("Main")
    DataUtils.createDir()
    ServerInstance.load()
    val serversThread = Thread {
        while (true) {
            ServerInstance.INSTANCE.forEach {
                it.updatePlayerList()
            }
            Thread.sleep(15 * 1000)
        }
    }
    serversThread.name = "ServersThread"
    serversThread.start()
    val scanner = Scanner(System.`in`)
    while (true) {
        cmd = scanner.nextLine()
    }
}