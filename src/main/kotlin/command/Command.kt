package command

import ServerInstance
import api.ApiBuilder
import api.Server
import com.google.gson.Gson
import data.GatewayServerSearch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

/**
 * @Description
 * @Author littleArray
 * @Date 2023/10/21
 */
object Command {
    private val loger: Logger = LoggerFactory.getLogger(this.javaClass)
    var cmd: String = ""
        set(value) {
            field = value
            cmd(value)
        }

    fun cmd(cmd:String){
        val split = cmd.split(" ")
        when(split[0]){
            "add" ->{
                val gameID = try {
                    split.getOrNull(1)?.toLong()
                } catch (e: Exception) {
                    loger.error("非法参数")
                    return
                }
                val sessionID = split.getOrNull(2)
                if (gameID == null || sessionID == null) {
                    loger.error("缺少参数")
                    return
                }
                if (ServerInstance.addServer(Server.ServerSetting(sessionID=sessionID, gameId = gameID))) {
                    loger.info("添加成功")
                }else{
                    loger.info("添加失败")
                }
            }
            "remove" ->{
                val gameID = try {
                    split.getOrNull(1)?.toLong()
                } catch (e: Exception) {
                    loger.error("非法参数")
                    return
                }
                if (gameID == null) {
                    loger.error("缺少参数")
                    return
                }
                if (ServerInstance.removeServer(gameID)) {
                    loger.info("移除成功")
                }else{
                    loger.info("移除失败")
                }
            }
            "search" ->{
                val reqBody = ApiBuilder.searchServer(split.getOrNull(1) ?: "", split.getOrNull(2) ?: ServerInstance.INSTANCE.firstOrNull()?.serverSetting?.sessionID ?:"").reqBody
                Gson().fromJson(reqBody,GatewayServerSearch::class.java).result.gameservers.forEach {
                    loger.info("服名:${it.name} GameID:${it.gameId} ${it.mapNamePretty} - ${it.mapModePretty}")
                }
            }
            "update" ->{

            }
            "stop" ->{
                ServerInstance.save()
                exitProcess(0)
            }
            else-> loger.info("无效命令")
        }
    }
}