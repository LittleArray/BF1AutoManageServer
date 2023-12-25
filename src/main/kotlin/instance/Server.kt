package instance

import api.*
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlComment
import com.google.gson.Gson
import data.GatewayServerSearch
import data.PostResponse
import data.Result
import kotlinx.serialization.Serializable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utils.DataUtils
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs
import kotlin.math.absoluteValue

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
    var playerList = CopyOnWriteArrayList<Player>()
    var oldGameID = 0L
    var lrcLog:LRCLog ?= null
    data class LRCLog(
        var id: String,
        var time: Long,
        var playTime: Long,
        var kick: Boolean,
        var msg:String
    )
    var soldier = 0
        set(value) {
            if ((field - value).absoluteValue > 2)
                loger.info("{} 士兵数量变更 {} -> {}", serverSetting.gameId, field, value)
            field = value
        }
    var spectator = 0
        set(value) {
            if (field != value)
                loger.info("{} 观战数量变更 {} -> {}", serverSetting.gameId, field, value)
            field = value
        }
    var queue = 0
        set(value) {
            if ((field - value).absoluteValue > 2)
                loger.info("{} 加载数量变更 {} -> {}", serverSetting.gameId, field, value)
            field = value
        }
    var bots = 0
        set(value) {
            if (field != value)
                loger.info("{} 机器人数量变更 {} -> {}", serverSetting.gameId, field, value)
            field = value
        }
    var cdPlayer = 0
        set(value) {
            if (field != value)
                loger.info("{} 踢出CD数量变更 {} -> {}", serverSetting.gameId, field, value)
            field = value
        }
    var progress = "0"
        set(value) {
            if (field != value)
                loger.info("{} 进度变更 {} -> {}", serverSetting.gameId, field, value)
            field = value
        }
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
                loger.info("{} 地图变更 {} -> {}", serverSetting.gameId, field, value)
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
        @YamlComment("服务器名")
        var name: String = "",
        @YamlComment("仅使用BFEAC踢挂")
        var onlyBFEAC: Boolean = false,
        @YamlComment("仅使用低等级数据检测")
        var onlyLRC: Boolean = true,
        @YamlComment(
            "启用低等级严管",
            "游戏时长小与30h的都会触发低等级管理机制",
            "如果误触发请加本工具的白名单",
            "默认启用"
        )
        var lowRankMan: Boolean = true,
        @YamlComment("自动踢出低等级数据检测")
        var lrcKick:Boolean = false,
        @YamlComment("踢条形码")
        var ilKick: Boolean = false,
        @YamlComment("对接接口的Token")
        var token: String = UUID.randomUUID().toString(),
        @YamlComment("服务器GameID")
        var gameId: Long = 0,
        @YamlComment("使用全局管服号")
        var opName: String = "",
        @YamlComment("管服账号的sessionID")
        var sessionID: String = "",
        @YamlComment("管服账号的sid")
        var sid: String = "",
        @YamlComment("管服账号的remid")
        var remid: String = "",
        @YamlComment("以下数据任意一个超过5.0的都不做判断", "服务器内玩家最高生涯KD")
        var lifeMaxKD: Double = 5.1,
        @YamlComment("服务器内玩家最高生涯KPM")
        var lifeMaxKPM: Double = 5.1,
        @YamlComment("服务器内玩家最高最近KPM")
        var recMaxKPM: Double = 5.1,
        @YamlComment(
            "服务器内玩家最高最近KD",
            "最近限制数据来自Btr,该功能是给noob服设计的,当然其他服也能用,一般情况下限制设置的比生涯限制高一点就行"
        )
        var recMaxKD: Double = 5.1,
        @YamlComment("在超过此游玩时长(单位:分钟)的对局中计算最近数据", "超过30不做判断", "已弃用")
        var recPlayTime: Double = 31.0,
        @YamlComment("最近数据超过这个击杀数后忽略游玩时长并计算最近数据")
        var matchKillsEnable: Int = 999,
        @YamlComment("计算最近对局有效数量")
        var recCount: Double = 3.0,
        @YamlComment("服内最多计算数,0不做限制")
        var killsMax: Int = 0,
        @YamlComment("踢出CD")
        var kickCD: Int = 0,
        @YamlComment("踢出高延迟,0为不限制")
        var maxPing: Int = 0,
        @YamlComment("如果超过这个数量的实体Ban则踢出")
        var tooManyBan: Int = 3,
        @YamlComment("胜率限制,0.0-1.0(小数非百分比)", "超过1不做判断")
        var winPercentLimited: Double = 1.1,
        @YamlComment("等级限制", "超过150不做判断")
        var rankLimited: Int = 151,
        @YamlComment("禁止快速重进服务器,默认关闭")
        var reEnterKick: Boolean = false,
        @YamlComment("禁止非管理观战,默认关闭")
        var spectatorKick: Boolean = false,
        @YamlComment("对应兵种等级限制,默认不限制")
        var classRankLimited: MutableMap<String, Int> = mutableMapOf(
            Pair("assault", 51),
            Pair("cavalry", 51),
            Pair("medic", 51),
            Pair("pilot", 51),
            Pair("tanker", 51),
        ),
        @YamlComment("战队限制,请填入战队完整名称,不是缩写")
        var platoonLimited: MutableSet<String> = mutableSetOf(),
        @YamlComment("白名单")
        var whitelist: MutableSet<String> = mutableSetOf(),
        @YamlComment("机器人白名单")
        var botlist: MutableSet<String> = mutableSetOf(),
        @YamlComment("VBan")
        var vbanlist: MutableSet<String> = mutableSetOf(),
        @YamlComment("管理员PID列表,不用管")
        var adminlist: MutableSet<String> = mutableSetOf(),
        @YamlComment("历史击杀列表")
        var killsMap: MutableMap<String, Int> = mutableMapOf()
    ) {
        /*override fun toString(): String {
            return "$gameId $lifeMaxKD $lifeMaxKPM $winPercentLimited $classRankLimited"
        }*/
    }

    fun saveServer() {
        if (loaderr) return
        try {
            DataUtils.save(
                "ServerSetting_${serverSetting.gameId}",
                Yaml.default.encodeToString(ServerSetting.serializer(), serverSetting)
            )
            loger.info("服务器 {} 数据保存成功", serverSetting.gameId)
        } catch (e: Exception) {
            loger.info("服务器 {} 数据保存失败  {} ", serverSetting.gameId, e.stackTraceToString())
        }
        if (oldGameID != serverSetting.gameId) {
            DataUtils.del("ServerSetting_${oldGameID}")
            loger.info("服务器 {} 删除旧数据完毕", oldGameID)
        }
    }

    fun loadServer() {
        try {
            serverSetting = Yaml.default.decodeFromString(
                ServerSetting.serializer(),
                DataUtils.load("ServerSetting_${serverSetting.gameId}")
            )
            loger.info("服务器 {} 数据载入成功", serverSetting.gameId)
            getRSPInfo()
            oldGameID = serverSetting.gameId
            //loger.info("服务器配置: {} ", serverSetting.toString())
        } catch (e: Exception) {
            loger.info("服务器 {} 数据载入失败  {} ", serverSetting.gameId, e.stackTraceToString())
            loaderr = true
            DataUtils.save(
                "ServerSetting_${serverSetting.gameId}.back",
                DataUtils.load("ServerSetting_${serverSetting.gameId}")
            )
        }
        if (serverSetting.opName.isNotEmpty()) {
            config.GConfig.Config.oplist.forEach {
                run {
                    if (it.name == serverSetting.opName) {
                        serverSetting.sessionID = it.sessionID
                        loger.info(" {} 使用全局管服号 {} ", serverSetting.gameId, serverSetting.opName)
                        return@run
                    }
                }
            }
        }
    }

    fun updateSessionID() {
        if (serverSetting.opName.isEmpty())
            serverSetting.sessionID =
                GatewayUtils.getSessionId(serverSetting.sid, serverSetting.remid) ?: serverSetting.sessionID
    }

    fun updateGameID() {
        loger.info("更新服务器GameID中 {}", serverSetting.gameId)
        val server = GatewayApi.searchServer(serverSetting.name, serverSetting.sessionID)
        if (!server.isSuccessful) return
        server.reqBody.let {
            Gson().fromJson(it, GatewayServerSearch::class.java).result.gameservers.firstOrNull()?.let {
                it.takeIf { it.name == serverSetting.name }?.let {
                    loger.info("更新服务器GameID成功 {} -> {}", serverSetting.gameId, it.gameId)
                    serverSetting.gameId = it.gameId.toLong()
                }
            }
        }
    }

    fun updatePlayerList() {
        if (serverSetting.gameId == 0L) return
        val list = PlayerListApi.getPlayerListBy22(serverSetting.gameId)
        if (!list.isSuccessful) return
        if (list.GDAT?.firstOrNull()?.GNAM == null) {
            updateGameID()
            return
        }
        serverSetting.name = list.GDAT.firstOrNull()?.GNAM ?:serverSetting.name
        mapName = list.GDAT.firstOrNull()?.ATTR?.level ?: ""
        val admin = (list.GDAT.firstOrNull()?.ATTR?.admins1 ?: "") +
                (list.GDAT.firstOrNull()?.ATTR?.admins2 ?: "") +
                (list.GDAT.firstOrNull()?.ATTR?.admins3 ?: "") +
                (list.GDAT.firstOrNull()?.ATTR?.admins4 ?: "")
        serverSetting.adminlist = admin.split(";").toMutableSet()
        var nsoldier = 0
        var nqueue = 0
        var nspectator = 0
        var nbots = 0
        var ncdPlayer = 0
        progress = list.GDAT.firstOrNull()?.ATTR?.progress ?: "0"
        val multiCheck = BFEACApi.MultiCheckPostJson()
        //玩家数量
        list.GDAT.firstOrNull()?.ROST?.forEach { p ->
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
                    if (it.nextEnterTime > 0) {//如果存在cd
                        if (System.currentTimeMillis() > it.nextEnterTime) {//如果超时
                            it.exit()
                            true
                        } else {
                            false
                        }
                    } else {
                        it.exit()
                        true
                    }
                } else {
                    false
                }
            }
            //机器人
            if (serverSetting.botlist.any { it == p.NAME } || config.GConfig.Config.botlist.any { it == p.NAME }) nbots++
            //真实玩家
            if (p.ROLE != "" && p.TIDX.toInt() != 65535) nsoldier++
            //加载中玩家
            if (p.TIDX.toInt() != 0 && p.TIDX.toInt() != 1) nqueue++
            //观战玩家
            if (p.ROLE == "") nspectator++
            //新玩家
            if (playerList.none { it.pid == p.PID }) {
                val newPlayer = Player(serverSetting.sessionID, p, this::serverSetting)
                playerList.add(newPlayer)
            }
            //老玩家
            run o@{
                playerList.forEach {
                    if (it.nextEnterTime > 0) ncdPlayer++
                    if (it._p.PID == p.PID && it._p != p) {
                        it.update(p)
                        return@o
                    }
                }
            }
        }
        soldier = nsoldier
        queue = nqueue
        bots = nbots
        spectator = nspectator
        cdPlayer = ncdPlayer
        if (multiCheck.pids.isEmpty()) return
        val multiCheckResponse = BFEACApi.multiCheck(multiCheck)
        multiCheckResponse.data.forEach { c ->
            playerList.forEach {
                if (it.pid == c) {
                    it.kick("Ban By BFEAC.COM")
                }
            }
        }

    }

    /**
     * 基于最近数据的队伍平衡(测试
     */
    fun balanceTeams() {
        loger.info("服务器  {}  开始平衡", serverSetting.gameId)
        // 将玩家按照KPM进行排序

        val team1 =
            playerList.filter { it._p.TIDX == 0L && it.rkd != null && it.rkp != null && serverSetting.botlist.none { wl -> wl == it._p.NAME } && config.GConfig.Config.botlist.none { wl -> wl == it._p.NAME } }
                .sortedByDescending { (it.rkp ?: 0.0) + (it.rkd ?: 0.0) }.toMutableList()
        val team2 =
            playerList.filter { it._p.TIDX == 1L && it.rkd != null && it.rkp != null && serverSetting.botlist.none { wl -> wl == it._p.NAME } && config.GConfig.Config.botlist.none { wl -> wl == it._p.NAME } }
                .sortedByDescending { (it.rkp ?: 0.0) + (it.rkd ?: 0.0) }.toMutableList()
        loger.info("队伍1有效数据量  {}  队伍2有效数据量  {} ", team1.size, team2.size)
        team1.forEach {
            loger.info(
                "队伍1 玩家  {}  RKD: {}  RKP: {} ",
                it._p.NAME,
                String.format("%.2f", it.rkd),
                String.format("%.2f", it.rkp)
            )
        }
        team2.forEach {
            loger.info(
                "队伍2 玩家  {}  RKD: {}  RKP: {} ",
                it._p.NAME,
                String.format("%.2f", it.rkd),
                String.format("%.2f", it.rkp)
            )
        }
        if (abs(team1.size - team2.size) > 7) {
            loger.info("队伍人数不平衡")
        }
    }

    fun getRSPInfo(): Result? {
        val rspInfo = GatewayApi.getFullServerDetails(serverSetting.sessionID, serverSetting.gameId.toString()).result
        serverSetting.rspId = rspInfo?.rspInfo?.server?.serverId?.toLong() ?: serverSetting.rspId
        serverSetting.name = rspInfo?.serverInfo?.name ?: serverSetting.name
        serverSetting.gameId = rspInfo?.serverInfo?.gameId?.toLong() ?: serverSetting.gameId
        rspInfo?.rspInfo?.bannedList?.forEach {
            serverSetting.vbanlist.add(it.personaId)
        }
        return rspInfo
    }

    fun movePlayer(pid: Long, teamID: Int): PostResponse {
        return GatewayApi.movePlayer(serverSetting.sessionID, serverSetting.gameId.toString(), pid, teamID)
    }

    fun chooseMap(index: Int): String {
        val result = getRSPInfo()
        val chooseServerMap = GatewayApi.chooseServerMap(
            serverSetting.sessionID,
            result?.rspInfo?.server?.persistedGameId ?: "",
            index.toString()
        )
        return if (chooseServerMap.isSuccessful) {
            result?.serverInfo?.rotation?.get(index)?.mapPrettyName ?: "图池中不存在该地图"
        } else {
            chooseServerMap.error
        }
    }

    fun getMap(): List<String>? {
        val result = getRSPInfo()
        return result?.serverInfo?.rotation?.map { it.mapPrettyName }
    }

    fun addVip(id: String): Boolean {
        val result = getRSPInfo()
        val vip = GatewayApi.addServerVIP(serverSetting.sessionID, result?.rspInfo?.server?.serverId?.toInt() ?: 0, id)
        if (vip.isSuccessful) loger.info("添加vip成功  {}   {} ", id, serverSetting.gameId)
        return vip.isSuccessful
    }

    fun removeVip(pid: String): Boolean {
        val result = getRSPInfo()
        val vip =
            GatewayApi.removeServerVIP(serverSetting.sessionID, result?.rspInfo?.server?.serverId?.toInt() ?: 0, pid)
        if (vip.isSuccessful) loger.info("移除vip成功  {}   {} ", pid, serverSetting.gameId)
        return vip.isSuccessful
    }

    fun addBan(id: String): Boolean {
        val rspInfo = getRSPInfo()
        val ban = GatewayApi.addServerBan(
            serverSetting.sessionID,
            rspInfo?.rspInfo?.server?.serverId?.toInt() ?: 0,
            id
        )
        if (ban.isSuccessful) loger.info(" {} 封禁成功", id)
        return ban.isSuccessful
    }

    fun removeBan(pid: String): Boolean {
        val rspInfo = getRSPInfo()
        val ban = GatewayApi.removeServerBan(
            serverSetting.sessionID,
            rspInfo?.rspInfo?.server?.serverId?.toInt() ?: 0,
            pid
        )
        if (ban.isSuccessful) loger.info(" {} 解禁成功", pid)
        serverSetting.vbanlist.remove(pid)
        return ban.isSuccessful
    }

    fun addVBan(id: String): Boolean {
        return serverSetting.vbanlist.add(id)
    }

    fun removeVban(pid: String): Boolean {
        return serverSetting.vbanlist.remove(pid)
    }
}