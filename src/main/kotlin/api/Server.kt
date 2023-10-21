package api

import com.google.gson.Gson
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utils.DataUtils

/**
 * @Description
 * @Author littleArray
 * @Date 2023/10/21
 */
class Server(var serverSetting: ServerSetting = ServerSetting()){
    private val loger: Logger = LoggerFactory.getLogger(this.javaClass)
    var playerList = mutableListOf<Player>()
    var soldier = 0
    var spectator = 0
    var queue = 0
    var mapName = ""
    var mapPretty = ""
    class ServerSetting (
        var rspId: Long = 0,
        var gameId: Long = 0,
        var sessionID: String = "",
        var lifeMaxKD: Double = 5.0,
        var lifeMaxKPM: Double = 5.0,
        var recMaxKPM: Double = 5.0,
        var recMaxKD: Double = 5.0,
        var weaponLimited: MutableList<String> = mutableListOf(),
        var vehicleLimited: MutableList<String> = mutableListOf(),
        var platoonLimited: MutableList<String> = mutableListOf(),
        var whitelist: MutableList<String> = mutableListOf(),
        var vbanlist: MutableList<String> = mutableListOf()
    )
    fun saveServer() {
        try {
            DataUtils.save("ServerSetting_${serverSetting.gameId}", Gson().toJson(serverSetting, ServerSetting::class.java))
            loger.info("服务器{}数据保存成功", serverSetting.gameId)
        } catch (e: Exception) {
            loger.info("服务器{}数据保存失败 {}", serverSetting.gameId, e.stackTraceToString())
        }
    }

    fun loadServer() {
        try {
            serverSetting = Gson().fromJson(DataUtils.load("ServerSetting_${serverSetting.gameId}"), ServerSetting::class.java)
            loger.info("服务器{}数据载入成功", serverSetting.gameId)
        } catch (e: Exception) {
            loger.info("服务器{}数据载入失败 {}", serverSetting.gameId, e.stackTraceToString())
        }
    }

    fun updatePlayerList() {
        if (serverSetting.gameId == 0L) return
        val list = ApiBuilder.getPlayerListBy22(serverSetting.gameId)
        if (!list.isSuccessful) return
        mapName = list.GDAT?.firstOrNull()?.ATTR?.level?:""
        if(list.GDAT?.firstOrNull()?.ROST == null || list.GDAT.firstOrNull()?.ROST?.isEmpty() == true){
            playerList = mutableListOf()
            loger.warn("服务器{}内玩家已清空",serverSetting.gameId)
        }
        soldier = 0
        queue = 0
        spectator = 0
        //玩家数量
        list.GDAT?.get(0)?.ROST?.forEach { p ->
            //真实玩家
            if (p.ROLE != "" && p.TIDX.toInt() != 65535) {
                soldier++
            }
            //加载中玩家
            if (p.TIDX.toInt() != 0 && p.TIDX.toInt() != 1) queue++
            //观战玩家
            if (p.ROLE == "") spectator++
            //新玩家
            if (playerList.none { it.pid == p.PID })
                playerList.add(Player(serverSetting.sessionID,p.PID,p,this::serverSetting))
            //老玩家
            playerList.forEach {
                if (it.pid == p.PID){
                    it.update(p)
                }
            }
        }
        loger.info("服务器{}玩家数量更新 玩家:{} 观战:{} 加载:{}",serverSetting.gameId,soldier,spectator,queue)
    }
}