package instance

import api.BFEACApi
import api.GatewayApi
import api.GatewayUtils
import api.PlayerListApi
import com.google.gson.Gson
import data.PostResponse
import data.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utils.DataUtils

/**
 * @Description
 * @Author littleArray
 * @Date 2023/10/21
 */
class Server(var serverSetting: ServerSetting = ServerSetting()) {
    val mapCache: HashMap<String, String> = hashMapOf(
        "MP_Desert" to "西奈沙漠",
        "MP_FaoFortress" to "法歐堡",
        "MP_Suez" to "蘇伊士",
        "MP_Argonne" to "阿爾貢森林",
        "MP_ItalianCoast" to "意大利海岸",
        "MP_Amiens" to "亞眠",
        "MP_MountainFort" to "拉粑粑山",
        "MP_Graveyard" to "決裂",
        "MP_Forest" to "阿爾貢森林",
        "MP_Verdun" to "老畢登高地",
        "MP_Underworld" to "垃圾要塞",
        "MP_Fields" to "蘇瓦松",
        "MP_Volga" to "伏爾加河",
        "MP_Tsaritsyn" to "察裏津",
        "MP_Harbor" to "澤布呂赫",
        "MP_Naval" to "黑爾戈蘭灣",
        "MP_Giant" to "龐然暗影",
        "MP_Ridge" to "迫擊巴巴",
        "MP_Chateau" to "流血宴廳",
        "MP_Offensive" to "索姆河",
        "MP_ShovelTown" to "尼維爾之夜",
        "MP_Bridge" to "勃魯西洛夫關口",
        "MP_Scar" to "聖康坦的傷痕",
    )
    val mapTeamName: HashMap<String, Pair<String, String>> = hashMapOf(
        "MP_Amiens" to Pair("大英帝國", "德意志帝國"),
        "MP_ItalianCoast" to Pair("奧匈帝國", "意大利王國"),
        "MP_ShovelTown" to Pair("法蘭西共和國", "德意志帝國"),
        "MP_MountainFort" to Pair("奧匈帝國", "意大利王國"),
        "MP_Graveyard" to Pair("德意志帝國", "法蘭西共和國"),
        "MP_FaoFortress" to Pair("奧斯曼帝國", "大英帝國"),
        "MP_Chateau" to Pair("德意志帝國", "美利堅合衆國"),
        "MP_Scar" to Pair("大英帝國", "德意志帝國"),
        "MP_Suez" to Pair("奧斯曼帝國", "大英帝國"),
        "MP_Desert" to Pair("奧斯曼帝國", "大英帝國"),
        "MP_Forest" to Pair("德意志帝國", "美利堅合衆國"),
        "MP_Giant" to Pair("大英帝國", "德意志帝國"),
        "MP_Verdun" to Pair("法蘭西共和國", "德意志帝國"),
        "MP_Trench" to Pair("法蘭西共和國", "德意志帝國"),
        "MP_Underworld" to Pair("法蘭西共和國", "德意志帝國"),
        "MP_Fields" to Pair("德意志帝國", "法蘭西共和國"),
        "MP_Valley" to Pair("奧匈帝國", "俄羅斯帝國"),
        "MP_Bridge" to Pair("奧匈帝國", "俄羅斯帝國"),
        "MP_Tsaritsyn" to Pair("白軍", "紅軍"),
        "MP_Ravines" to Pair("俄羅斯帝國", "奧匈帝國"),
        "MP_Volga" to Pair("白軍", "紅軍"),
        "MP_Islands" to Pair("俄羅斯帝國", "德意志帝國"),
        "MP_Beachhead" to Pair("奧斯曼帝國", "大英帝國"),
        "MP_Harbor" to Pair("德意志帝國", "皇家海軍陸戰隊"),
        "MP_Ridge" to Pair("奧斯曼帝國", "大英帝國"),
        "MP_River" to Pair("意大利王國", "奧匈帝國"),
        "MP_Hell" to Pair("德意志帝國", "大英帝國"),
        "MP_Offensive" to Pair("德意志帝國", "大英帝國"),
        "MP_Naval" to Pair("德意志帝國", "皇家海軍陸戰隊"),
        "MP_Blitz" to Pair("大英帝國", "德意志帝國"),
        "MP_London" to Pair("大英帝國", "德意志帝國"),
        "MP_Alps" to Pair("大英帝國", "德意志帝國"),
    )
    private val loger: Logger = LoggerFactory.getLogger(this.javaClass)
    var playerList = mutableListOf<Player>()
    var soldier = 0
    var spectator = 0
    var queue = 0
    var mapName = ""
        set(value) {
            mapCache.forEach { name, pre ->
                if (value == name) mapPretty = pre
            }
            field = value
        }
    var mapPretty = ""
        set(value) {
            val old = field
            if (old != value) {
                loger.info("服务器{}地图变更:{}", serverSetting.gameId, value)
            }
            field = value
        }
    class ServerSetting(
        var rspId: Long = 0,
        var gameId: Long = 0,
        var sessionID: String = "",
        var sid: String = "",
        var remid: String = "",
        var lifeMaxKD: Double = 5.0,
        var lifeMaxKPM: Double = 5.0,
        var recMaxKPM: Double = 5.0,
        var recMaxKD: Double = 5.0,
        var kickCD: Int = 0,
        var killsLimited: Int = 999,
        var matchKillsEnable: Int = 999,
        var matchKDLimited: Double = 5.0,
        var winPercentLimited: Double = 1.1,
        var rankLimited: Int = 151,
        var reEnterKick: Boolean = false,
        var spectatorKick: Boolean = false,
        var classRankLimited: MutableMap<String, Int> = mutableMapOf(),
        var weaponLimited: MutableList<String> = mutableListOf(),
        var weaponStarLimited: Int = 9999,
        var vehicleLimited: MutableList<String> = mutableListOf(),
        var vehicleStarLimited: Int = 9999,
        var platoonLimited: MutableList<String> = mutableListOf(),
        var whitelist: MutableList<String> = mutableListOf(),
        var botlist: MutableList<String> = mutableListOf(),
        var vbanlist: MutableList<String> = mutableListOf(),
        var adminlist: MutableList<String> = mutableListOf()
    ) {
        override fun toString(): String {
            return "$gameId $lifeMaxKD $lifeMaxKPM $winPercentLimited $classRankLimited"
        }
    }

    fun saveServer() {
        try {
            DataUtils.save(
                "ServerSetting_${serverSetting.gameId}",
                Gson().toJson(serverSetting, ServerSetting::class.java)
            )
            loger.info("服务器{}数据保存成功", serverSetting.gameId)
        } catch (e: Exception) {
            loger.info("服务器{}数据保存失败 {}", serverSetting.gameId, e.stackTraceToString())
        }
    }

    fun loadServer() {
        try {
            serverSetting =
                Gson().fromJson(DataUtils.load("ServerSetting_${serverSetting.gameId}"), ServerSetting::class.java)
            loger.info("服务器{}数据载入成功", serverSetting.gameId)
            loger.info("服务器配置:{}", serverSetting.toString())
        } catch (e: Exception) {
            loger.info("服务器{}数据载入失败 {}", serverSetting.gameId, e.stackTraceToString())
        }
    }

    fun updateSessionID() {
        serverSetting.sessionID =
            GatewayUtils.getSessionId(serverSetting.sid, serverSetting.remid) ?: serverSetting.sessionID
    }

    fun updatePlayerList() {
        if (serverSetting.gameId == 0L) return
        val list = PlayerListApi.getPlayerListBy22(serverSetting.gameId)
        if (!list.isSuccessful) return
        mapName = list.GDAT?.firstOrNull()?.ATTR?.level ?: ""
        if (list.GDAT?.firstOrNull()?.ROST == null || list.GDAT.firstOrNull()?.ROST?.isEmpty() == true) {
            playerList = mutableListOf()
            loger.warn("服务器{}内玩家已清空", serverSetting.gameId)
        }
        val admin = (list.GDAT?.firstOrNull()?.ATTR?.admins1?:"") +
                (list.GDAT?.firstOrNull()?.ATTR?.admins2?:"") +
                (list.GDAT?.firstOrNull()?.ATTR?.admins3?:"") +
                (list.GDAT?.firstOrNull()?.ATTR?.admins4?:"")
        serverSetting.adminlist = admin.split(";").toMutableList()
        soldier = 0
        queue = 0
        spectator = 0
        var bots = 0
        var cdPlayer = 0
        val multiCheck = BFEACApi.MultiCheckPostJson()
        //玩家数量
        list.GDAT?.firstOrNull()?.ROST?.forEach { p ->
            multiCheck.pids.add(p.PID)
            mapTeamName.forEach { mp, tn ->
                if (mapName == mp) {
                    if (p.TIDX == 0L) {
                        p.TEAMNAME = tn.first
                    } else if (p.TIDX == 1L) {
                        p.TEAMNAME = tn.second
                    }
                }
            }
            //离开玩家
            playerList.removeIf {
                if (list.GDAT[0].ROST.none { e -> e.PID == it.pid }) {
                    if(it.nextEnterTime > 0){
                        if (System.currentTimeMillis() > it.nextEnterTime){
                            it.exit()
                            true
                        }else{
                            false
                        }
                    }else{
                        it.exit()
                        true
                    }
                } else {
                    false
                }
            }
            //机器人
            if (serverSetting.botlist.any { it == p.NAME }) bots++
            //真实玩家
            if (p.ROLE != "" && p.TIDX.toInt() != 65535) soldier++
            //加载中玩家
            if (p.TIDX.toInt() != 0 && p.TIDX.toInt() != 1) queue++
            //观战玩家
            if (p.ROLE == "") spectator++
            //新玩家
            if (playerList.none { it.pid == p.PID }) {
                val newPlayer = Player(serverSetting.sessionID, p, this::serverSetting,mapPretty)
                playerList.add(newPlayer)
            }
            //老玩家
            playerList.forEach {
                if (it.pid == p.PID) {
                    it.update(p,mapPretty)
                }
            }
        }
        playerList.forEach {
            if (it.nextEnterTime > 0) cdPlayer++
        }
        loger.info(
            "服务器{}玩家数量更新 玩家:{} 观战:{} 加载:{} 机器人:{} 踢出CD玩家:{} 总数:{} 进度:{}",
            serverSetting.gameId,
            soldier,
            spectator,
            queue,
            bots,
            cdPlayer,
            playerList.size,
            list.GDAT?.firstOrNull()?.ATTR?.progress
        )
        if (multiCheck.pids.isEmpty()) return
        val multiCheckResponse = BFEACApi.multiCheck(multiCheck)
        multiCheckResponse.data.forEach { c ->
            playerList.forEach {
                if (it.pid == c) it.kick("Ban By BFEAC.COM")
            }
        }
    }
    fun getRSPInfo(): Result? {
        return GatewayApi.getFullServerDetails(serverSetting.sessionID, serverSetting.gameId.toString()).result
    }
    fun chooseMap(index:Int): String? {
        val result = getRSPInfo()
        return if (GatewayApi.chooseServerMap(serverSetting.sessionID, result?.rspInfo?.server?.persistedGameId ?: "", index.toString()).isSuccessful) {
            result?.serverInfo?.rotation?.get(index)?.mapPrettyName
        }else{
            null
        }
    }
}