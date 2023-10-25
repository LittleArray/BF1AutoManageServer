package instance

import api.GatewayApi
import com.google.gson.Gson
import data.*
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import kotlin.reflect.KMutableProperty0

/**
 * @Description
 * @Author littleArray
 * @Date 2023/10/7
 */
class Player(
    val sessionId: String,
    p: PLBy22.ROST,
    var serverSetting: KMutableProperty0<Server.ServerSetting>,
    map:String,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val loger: Logger = LoggerFactory.getLogger(this.javaClass)
    val pid: Long = p.PID
    var _p: PLBy22.ROST = p
        set(value) {
            val old = field
            if (old.JGTS != value.JGTS) {//加入时间变更了
                loger.info("玩家{}进服时间变更", value.NAME)
                init()
            }
            if (old.ROLE != value.ROLE) {//身份变更
                loger.info("玩家{}身份变更 {}", value.NAME, value.ROLE)
            }
            if (old.TIDX != value.TIDX) {//队伍变更
                loger.info("玩家{}队伍变更 {}", value.NAME, value.TEAMNAME)
            }
            field = value
        }
    var mapPretty:String = map
        set(value) {
            val old = field
            if (old != value){
                //地图更新
            }
            field = value
        }
    var teamName:String = ""
    var isExit = false
    var nextEnterTime = 0L
    var oldWpData : MutableMap<String,Int> = mutableMapOf()
    var oldVpData : MutableMap<String,Int> = mutableMapOf()

    init {
        init()
    }

    fun init() {
        loger.info("新玩家{}进入服务器{}", _p.NAME, serverSetting.get().gameId)
        isExit = false
        if (nextEnterTime > 0){
            if (System.currentTimeMillis() < nextEnterTime){
                kick("You can re-enter after ${SimpleDateFormat("HH:mm").format(nextEnterTime)}")
            }else{
                nextEnterTime = 0
            }
        }
        if (serverSetting.get().whitelist.any { wl-> wl == _p.NAME }) return
        if (serverSetting.get().botlist.any { wl-> wl == _p.NAME }) return
        coroutineScope.launch {
            kickAlwaysByStats()
            kickAlwaysByClassRank()
            randomWpCheck()
        }
    }

    suspend fun randomWpCheck(isAlwaysCheck:Boolean = false,times:Int = 0){
        if (isExit) return
        //loger.info("随机查询{}武器",_p.NAME)
        val weapons = getWeapons()
        weapons?.result?.forEach {wp->
            wp.weapons.forEach {
                val wps = it.stats.values
                val name = it.name
                val kickName = if (name.length>12) name.subSequence(0,12) else name
                val kills = wps?.kills?:0
                val shots = wps?.shots?:kills
                if (serverSetting.get().weaponLimited.any { wpl -> wpl == name }){
                    //loger.info("{}受限武器{}记录,当前击杀{},开火次数{}",_p.NAME, name,wps?.kills,wps?.shots)
                    if(oldWpData[name] != null && shots > oldWpData[name]!!){
                        kick("Ban $kickName",15)
                    }
                    oldWpData[name] = shots
                }
                if ((wps?.kills?.div(100)?:0) > serverSetting.get().weaponStarLimited){
                    //loger.info("{}的{}大于{}星,当前击杀{},开火次数{}",_p.NAME, name,serverSetting.get().weaponStarLimited,wps?.kills,wps?.shots)
                    if (wps?.kills != null){
                        if(oldWpData[name] != null && shots > oldWpData[name]!!){
                            kick("$kickName Over ${serverSetting.get().weaponStarLimited} Stars",15)
                        }
                        oldWpData[name] = shots
                    }
                }
            }
        }
        if(weapons != null) return
        if (isAlwaysCheck && times<5) {
            delay(60 * 1000)
            randomWpCheck(true,times+1)
        }
    }

    private suspend fun kickAlwaysByStats() {
        if (isExit) return
        val stats = getStats()
        if (stats != null) {
            //loger.info("玩家{}生涯数据:KD:{}", _p.NAME, stats.result.kdr)
            val winPercent = stats.result.basicStats.wins.toDouble() / (stats.result.basicStats.wins + stats.result.basicStats.losses)
            if (stats.result.basicStats.kpm > serverSetting.get().lifeMaxKPM)
                kick("LifeKPM Limited ${serverSetting.get().lifeMaxKD}")
            if (stats.result.kdr > serverSetting.get().lifeMaxKD)
                kick("LifeKD Limited ${serverSetting.get().lifeMaxKD}")
            if (winPercent > serverSetting.get().winPercentLimited)
                kick("WinPercent Limited ${serverSetting.get().winPercentLimited * 100}%")
        } else {
            loger.error("请求玩家{}生涯失败,5s后重新查询", _p.NAME)
            delay(5000)
            kickAlwaysByStats()
        }
    }

    private suspend fun kickAlwaysByClassRank() {
        if (isExit) return
        val classRank = getClassRank()
        if (classRank != null) {
            if (classRank.result.assault.rank > (serverSetting.get().classRankLimited["assault"] ?: 51))
                kick("AssaultRank Limited ${serverSetting.get().classRankLimited["assault"]}")
            if (classRank.result.cavalry.rank > (serverSetting.get().classRankLimited["cavalry"] ?: 51))
                kick("CavalryRank Limited ${serverSetting.get().classRankLimited["cavalry"]}")
            if (classRank.result.medic.rank > (serverSetting.get().classRankLimited["medic"] ?: 51))
                kick("MedicRank Limited ${serverSetting.get().classRankLimited["medic"]}")
            if (classRank.result.pilot.rank > (serverSetting.get().classRankLimited["pilot"] ?: 51))
                kick("PilotRank Limited ${serverSetting.get().classRankLimited["pilot"]}")
            if (classRank.result.tanker.rank > (serverSetting.get().classRankLimited["tanker"] ?: 51))
                kick("TankerRank Limited ${serverSetting.get().classRankLimited["tanker"]}")
        } else {
            loger.error("请求玩家{}兵种等级失败,5s后重新查询", _p.NAME)
            delay(5000)
            kickAlwaysByClassRank()
        }

    }

    fun update(p: PLBy22.ROST,map:String) {
        _p = p
        mapPretty = map
        //serverSetting = setting
    }

    fun exit() {
        loger.info("玩家{}离开服务器{}", _p.NAME, serverSetting.get().gameId)
        isExit = true
    }

    /**
     * 获取玩家兵种等级
     * @return GatewayClassRank?
     */
    private fun getClassRank(): GatewayClassRank? {
        val method = "Progression.getKitRanksMapByPersonaId"
        val body = Gson().toJson(
            JsonRpcObj(
                method = method,
                params = object {
                    val game = "tunguska"
                    val personaId = pid.toString()
                }
            )
        )
        val builder = GatewayApi.jsonRpc(body, sessionId)
        return try {
            Gson().fromJson(builder.reqBody, GatewayClassRank::class.java)
        } catch (e: Exception) {
            loger.error("获取{}玩家兵种等级失败,{}", pid, e.stackTraceToString())
            null
        }

    }

    /**
     * 获取生涯数据
     * @return Boolean
     */
    private fun getStats(): GatewayStats? {
        val method = "Stats.detailedStatsByPersonaId"
        val body = Gson().toJson(
            JsonRpcObj(
                method = method,
                params = object {
                    val game = "tunguska"
                    val personaId = pid.toString()
                }
            )
        )
        val builder = GatewayApi.jsonRpc(body, sessionId)
        if (builder.isSuccessful) {
            return try {
                Gson().fromJson(builder.reqBody, GatewayStats::class.java)
            } catch (e: Exception) {
                loger.error("获取{}玩家生涯数据失败,{}", pid, e.stackTraceToString())
                null
            }
            /*val bestcm = hashMapOf(
                "Assault" to "突擊兵",
                "Scout" to "偵察兵",
                "Medic" to "醫護兵",
                "Support" to "支援兵",
                "Tanker" to "坦克",
                "Cavalry" to "騎兵",
                "Pilot" to "駕駛員",
            )
            bestcm.forEach { t, u ->
                if (json.result.favoriteClass == t)
                    pbInfo.bestClass = u
            }
            val cls: MutableList<PlayerBaseInfo.Classes> = mutableListOf()
            val classRank = getClassRank()
            json.result.kitStats.forEach {
                cls.add(
                    PlayerBaseInfo.Classes(
                        className = it.prettyName,
                        score = it.score,
                        kills = it.kills,
                        secondsPlayed = it.secondsAs,
                        classRank = when(it.prettyName){
                            "突擊兵"->classRank?.result?.assault?.rank?:0
                            "偵察兵"->classRank?.result?.scout?.rank?:0
                            "醫護兵"->classRank?.result?.medic?.rank?:0
                            "支援兵"->classRank?.result?.support?.rank?:0
                            "坦克"->classRank?.result?.tanker?.rank?:0
                            "騎兵"->classRank?.result?.cavalry?.rank?:0
                            "駕駛員"->classRank?.result?.pilot?.rank?:0
                            else->0
                        }
                    )
                )
            }
            val gme: MutableList<PlayerBaseInfo.Gamemode> = mutableListOf()
            json.result.gameModeStats.forEach {
                gme.add(
                    PlayerBaseInfo.Gamemode(
                        modeName = it.prettyName,
                        wins = it.wins,
                        losses = it.losses,
                        winPercent = if (it.wins.toDouble() / (it.wins + it.losses) > 0) it.wins.toDouble() / (it.wins + it.losses) else 0.0,
                        score = it.score
                    )
                )
            }
            pbInfo.classes = cls
            pbInfo.gamemodes = gme*/

        }
        return null
    }

    /**
     * 获取武器数据
     * @return PlayerWeapons
     */
    fun getWeapons(): GatewayWeapons? {
        val method = "Progression.getWeaponsByPersonaId"
        val body = Gson().toJson(
            JsonRpcObj(
                method = method,
                params = object {
                    val game = "tunguska"
                    val personaId = pid.toString()
                }
            )
        )
        val builder = GatewayApi.jsonRpc(body, sessionId)
        return try {
            Gson().fromJson(builder.reqBody, GatewayWeapons::class.java)
        } catch (e: Exception) {
            loger.error("获取{}玩家武器数据失败,{}", pid, e.stackTraceToString())
            null
        }
    }

    /**
     * 获取战队列表
     * @return Boolean
     */
    private fun getPlatoons(): GatewayPlatoons? {
        val method = "Platoons.getPlatoons"
        val body = Gson().toJson(
            JsonRpcObj(
                method = method,
                params = object {
                    val personaId = pid.toString()
                }
            )
        )
        val builder = GatewayApi.jsonRpc(body, sessionId)
        return try {
            Gson().fromJson(builder.reqBody, GatewayPlatoons::class.java)
        } catch (e: Exception) {
            loger.error("获取{}玩家战队列表失败,{}", pid, e.stackTraceToString())
            null
        }
    }

    /**
     * 获取当前代表战队
     * @return Boolean
     */
    private fun getActivePlatoon(): GatewayActivePlatoon? {
        val method = "Platoons.getActivePlatoon"
        val body = Gson().toJson(
            JsonRpcObj(
                method = method,
                params = object {
                    val personaId = pid.toString()
                }
            )
        )
        val builder = GatewayApi.jsonRpc(body, sessionId)
        return try {
            Gson().fromJson(builder.reqBody, GatewayActivePlatoon::class.java)
        } catch (e: Exception) {
            loger.error("获取{}玩家当前代表战队失败,{}", pid, e.stackTraceToString())
            null
        }

    }

    /**
     * 获取最近游玩服务器
     * @return GatewayRecentServers?
     */
    private fun getMostRecentServers(): GatewayRecentServers? {
        val method = "ServerHistory.mostRecentServers"
        val body = Gson().toJson(
            JsonRpcObj(
                method = method,
                params = object {
                    val game = "tunguska"
                    val personaId = pid.toString()
                }
            )
        )
        val builder = GatewayApi.jsonRpc(body, sessionId)
        return try {
            Gson().fromJson(builder.reqBody, GatewayRecentServers::class.java)
        } catch (e: Exception) {
            loger.error("获取{}玩家最近游玩数据失败,{}", pid, e.stackTraceToString())
            null
        }
    }

    /**
     * 获取载具数据
     * @return String
     */
    fun getVehicles(): GatewayVehicles? {
        val method = "Progression.getVehiclesByPersonaId"
        val body = Gson().toJson(
            JsonRpcObj(
                method = method,
                params = object {
                    val game = "tunguska"
                    val personaId = pid.toString()
                }
            )
        )
        val builder = GatewayApi.jsonRpc(body, sessionId)

        return try {
            Gson().fromJson(builder.reqBody, GatewayVehicles::class.java)
        } catch (e: Exception) {
            loger.error("获取{}玩家载具数据数据失败,{}", pid, e.stackTraceToString())
            null
        }
    }

    fun kick(reason: String = "kick without reason",kickCD:Int = 0) {
        val kickPlayer = GatewayApi.kickPlayer(sessionId, serverSetting.get().gameId.toString(), pid.toString(), reason)
        if (kickPlayer.reqBody.contains("Error", true)) {
            loger.error("在服务器{}踢出玩家{}失败 {}", serverSetting.get().gameId.toString(), _p.NAME, kickPlayer.reqBody)
        } else {
            loger.info("在服务器{}踢出玩家{}成功,理由:{}", serverSetting.get().gameId.toString(), _p.NAME, reason)
            if (kickCD>0) nextEnterTime = System.currentTimeMillis() + (kickCD * 60 * 1000)
        }
    }
}