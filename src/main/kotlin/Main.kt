import api.QQBotApi
import command.Command.cmd
import config.Config
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
    Config.loadConfig()
    ServerInstance.load()
    val serversThread = Thread {
        while (true) {
            ServerInstance.INSTANCE.forEach {
                it.updatePlayerList()
            }
            Thread.sleep(15 * 1000)
        }
    }
    //QQBotApi.init(2086)
    serversThread.name = "ServersThread"
    serversThread.start()
    println("""
        欢迎使用BF1自动管服工具 1.0-preview
        项目地址:https://github.com/LittleArray/BF1AutoManageServer
        有问题请发issue
        请在控制台输入help查看帮助,关闭请用stop命令,不要使用 ^c
    """.trimIndent())
    val scanner = Scanner(System.`in`)
    while (true) {
        cmd = scanner.nextLine()
    }
}