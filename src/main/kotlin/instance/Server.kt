package instance

import api.*
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlComment
import com.google.gson.Gson
import command.Command
import data.PostResponse
import data.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.InheritableSerialInfo
import kotlinx.serialization.SerialInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utils.ChineseTR.toSimplified
import utils.DataUtils
import java.util.UUID

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
    var loaderr = false
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
                playerList.forEach {
                    it.isChangeMap = true
                    it.map = mapName
                }
            }
            field = value
        }
    @Serializable
    data class ServerSetting(
        @YamlComment("服务器RSPID")
        var rspId: Long = 0,
        @YamlComment("仅使用BFEAC踢挂")
        var onlyBFEAC : Boolean = false,
        @YamlComment("对接接口的Token(未开放)")
        var token: String = UUID.randomUUID().toString(),
        @YamlComment("服务器GameID")
        var gameId: Long = 0,
        @YamlComment("管服账号的sessionID")
        var sessionID: String = "",
        @YamlComment("管服账号的sid")
        var sid: String = "",
        @YamlComment("管服账号的remid")
        var remid: String = "",
        @YamlComment("以下数据任意一个超过5.0的都不做判断","服务器内玩家最高生涯KD")
        var lifeMaxKD: Double = 5.1,
        @YamlComment("服务器内玩家最高生涯KPM")
        var lifeMaxKPM: Double = 5.1,
        @YamlComment("服务器内玩家最高最近KPM")
        var recMaxKPM: Double = 5.1,
        @YamlComment("服务器内玩家最高最近KD","最近限制数据来自Btr,该功能是给noob服设计的,当然其他服也能用,一般情况下限制设置的比生涯限制高一点就行")
        var recMaxKD: Double = 5.1,
        @YamlComment("在超过此游玩时长(单位:分钟)的对局中计算最近数据","超过30不做判断")
        var recPlayTime: Double = 31.0,
        @YamlComment("最近数据超过这个击杀数后忽略游玩时长并计算最近数据")
        var matchKillsEnable: Int = 999,
        @YamlComment("计算最近对局的数量","数量越多踢得人越多,但是更能防止捞逼")
        var recCount: Int = 3,
        @YamlComment("踢出CD")
        var kickCD: Int = 0,
        @YamlComment("如果超过这个数量的实体Ban则踢出")
        var tooManyBan: Int = 3,
        @YamlComment("启用低等级严管","游戏时长小与30h的都会触发低等级管理机制","如果误触发请加本工具的白名单","默认启用")
        var lowRankMan: Boolean = true,
        @YamlComment("胜率限制,0.0-1.0(小数非百分比)","超过1不做判断")
        var winPercentLimited: Double = 1.1,
        @YamlComment("等级限制","超过150不做判断")
        var rankLimited: Int = 151,
        @YamlComment("禁止快速重进服务器,默认关闭")
        var reEnterKick: Boolean = false,
        @YamlComment("禁止非管理观战,默认关闭")
        var spectatorKick: Boolean = false,
        @YamlComment("对应兵种等级限制,默认不限制")
        var classRankLimited: MutableMap<String, Int> = mutableMapOf(
            Pair("assault",51),
            Pair("cavalry",51),
            Pair("medic",51),
            Pair("pilot",51),
            Pair("tanker",51),
        ),
        @YamlComment("战队限制,请填入战队完整名称,不是缩写")
        var platoonLimited: MutableList<String> = mutableListOf(),
        @YamlComment("白名单")
        var whitelist: MutableList<String> = mutableListOf(),
        @YamlComment("机器人白名单")
        var botlist: MutableList<String> = mutableListOf(),
        @YamlComment("VBan")
        var vbanlist: MutableList<String> = mutableListOf(),
        @YamlComment("管理员PID列表,不用管")
        var adminlist: MutableList<String> = mutableListOf()
    ) {
        /*override fun toString(): String {
            return "$gameId $lifeMaxKD $lifeMaxKPM $winPercentLimited $classRankLimited"
        }*/
    }

    fun saveServer() {
        val serializer = ServerSetting.serializer()
        if (loaderr) return
        try {
            DataUtils.save("ServerSetting_${serverSetting.gameId}", Yaml.default.encodeToString(ServerSetting.serializer(),serverSetting))
            loger.info("服务器{}数据保存成功", serverSetting.gameId)
        } catch (e: Exception) {
            loger.info("服务器{}数据保存失败 {}", serverSetting.gameId, e.stackTraceToString())
        }
    }

    fun loadServer() {
        try {
            serverSetting = Yaml.default.decodeFromString(ServerSetting.serializer(),DataUtils.load("ServerSetting_${serverSetting.gameId}"))
            loger.info("服务器{}数据载入成功", serverSetting.gameId)
            loger.info("服务器配置:{}", serverSetting.toString())
        } catch (e: Exception) {
            loger.info("服务器{}数据载入失败 {}", serverSetting.gameId, e.stackTraceToString())
            loaderr = true
            DataUtils.save("ServerSetting_${serverSetting.gameId}.back", DataUtils.load("ServerSetting_${serverSetting.gameId}"))
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
        val admin = (list.GDAT?.firstOrNull()?.ATTR?.admins1?:"") +
                (list.GDAT?.firstOrNull()?.ATTR?.admins2?:"") +
                (list.GDAT?.firstOrNull()?.ATTR?.admins3?:"") +
                (list.GDAT?.firstOrNull()?.ATTR?.admins4?:"")
        serverSetting.adminlist = admin.split(";").toMutableList()
        val oldSoldier = soldier
        val oldQueue = queue
        val oldSpectator = spectator
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
                    if(it.nextEnterTime > 0){//如果存在cd
                        if (System.currentTimeMillis() > it.nextEnterTime){//如果超时
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
                val newPlayer = Player(serverSetting.sessionID, p, this::serverSetting)
                playerList.add(newPlayer)
            }
            //老玩家
            playerList.forEach {
                if (it.pid == p.PID) {
                    it.update(p)
                }
            }
        }
        playerList.forEach {
            if (it.nextEnterTime > 0) cdPlayer++
        }
        if (oldSoldier != soldier || oldQueue != queue || oldSpectator != spectator){
            if (list.GDAT?.firstOrNull()?.ROST == null || list.GDAT.firstOrNull()?.ROST?.isEmpty() == true) {
                playerList = mutableListOf()
                loger.warn("服务器{}内玩家已清空", serverSetting.gameId)
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
        }
        if (multiCheck.pids.isEmpty()) return
        val multiCheckResponse = BFEACApi.multiCheck(multiCheck)
        multiCheckResponse.data.forEach { c ->
            playerList.forEach {
                if (it.pid == c) it.kick("Ban By BFEAC.COM")
            }
        }
    }
    fun getRSPInfo(): Result? {
        val rspInfo = GatewayApi.getFullServerDetails(serverSetting.sessionID, serverSetting.gameId.toString()).result
        serverSetting.rspId = rspInfo?.rspInfo?.server?.serverId?.toLong()?:0L
        return rspInfo
    }
    fun chooseMap(index:Int): String? {
        val result = getRSPInfo()
        return if (GatewayApi.chooseServerMap(serverSetting.sessionID, result?.rspInfo?.server?.persistedGameId ?: "", index.toString()).isSuccessful) {
            result?.serverInfo?.rotation?.get(index)?.mapPrettyName
        }else{
            null
        }
    }
    fun getMap():List<String>{
        val result = getRSPInfo()
        val list = mutableListOf<String>()
        result?.serverInfo?.rotation?.forEach {
            list.add(it.mapPrettyName.toSimplified())
        }
        return list
    }
    fun addVip(id:String): Boolean {
        val result = getRSPInfo()
        val vip = GatewayApi.addServerVIP(serverSetting.sessionID, result?.rspInfo?.server?.serverId?.toInt() ?: 0, id)
        if (vip.isSuccessful) loger.info("添加vip成功 {} {}",id,serverSetting.gameId)
        return vip.isSuccessful
    }
    fun removeVip(pid:String): Boolean {
        val result = getRSPInfo()
        val vip = GatewayApi.removeServerVIP(serverSetting.sessionID, result?.rspInfo?.server?.serverId?.toInt() ?: 0, pid)
        if (vip.isSuccessful) loger.info("移除vip成功 {} {}",pid,serverSetting.gameId)
        return vip.isSuccessful
    }
    fun addBan(id:String): Boolean {
        val rspInfo = getRSPInfo()
        val ban = GatewayApi.addServerBan(
            serverSetting.sessionID,
            rspInfo?.rspInfo?.server?.serverId?.toInt() ?: 0,
            id
        )
        if (ban.isSuccessful) loger.info("{}封禁成功", id)
        return ban.isSuccessful
    }
    fun removeBan(pid: String):Boolean{
        val rspInfo = getRSPInfo()
        val ban = GatewayApi.removeServerBan(
            serverSetting.sessionID,
            rspInfo?.rspInfo?.server?.serverId?.toInt() ?: 0,
            pid
        )
        if (ban.isSuccessful) loger.info("{}解禁成功", pid)
        return ban.isSuccessful
    }
    fun addVBan(id: String): Boolean {
        return serverSetting.vbanlist.add(id)
    }
    fun removeVban(id: String):Boolean{
        return serverSetting.vbanlist.remove(id)
    }
}