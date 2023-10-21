package api

import com.google.gson.Gson
import data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KMutableProperty0

/**
 * @Description
 * @Author littleArray
 * @Date 2023/10/7
 */
class Player(
    val sessionId: String,
    val pid: Long,
    p: PLBy22.ROST,
    var serverSetting: KMutableProperty0<Server.ServerSetting>,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val loger: Logger = LoggerFactory.getLogger(this.javaClass)
    var _p: PLBy22.ROST = p
        set(value) {
            val old = field
            if (old.JGTS != value.JGTS){//加入时间变更了
                loger.info("玩家{}进服时间变更",value.NAME)
            }
            if (old.ROLE != value.ROLE){//身份变更
                loger.info("玩家{}身份变更 {}",value.NAME,value.ROLE)
            }
            if (old.TIDX != value.TIDX){//队伍变更
                loger.info("玩家{}队伍变更 {}",value.NAME,value.TIDX)
            }
            field = value
        }

    init {
        kickAlwaysByStats()
    }

    fun kickAlwaysByStats(){
        coroutineScope.launch {
            val stats = getStats()
            if (stats != null) {
                if (stats.result.basicStats.kpm > serverSetting.get().lifeMaxKPM) kick(serverSetting.get().gameId,"Life KPM Limited")
                if (stats.result.kdr > serverSetting.get().lifeMaxKD) kick(serverSetting.get().gameId,"Life KD Limited")
            }else{
                kickAlwaysByStats()
            }
        }
    }
    fun update(p: PLBy22.ROST) {
        _p = p
        //serverSetting = setting
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
        val builder = ApiBuilder.jsonRpc(body, sessionId)
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
        val builder = ApiBuilder.jsonRpc(body, sessionId)
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
        val builder = ApiBuilder.jsonRpc(body, sessionId)
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
        val builder = ApiBuilder.jsonRpc(body, sessionId)
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
        val builder = ApiBuilder.jsonRpc(body, sessionId)
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
        val builder = ApiBuilder.jsonRpc(body, sessionId)
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
        val builder = ApiBuilder.jsonRpc(body, sessionId)

        return try {
            Gson().fromJson(builder.reqBody, GatewayVehicles::class.java)
        } catch (e: Exception) {
            loger.error("获取{}玩家载具数据数据失败,{}", pid, e.stackTraceToString())
            null
        }
    }

    fun kick(gameID: Long, reason: String = "kick without reason") {
        val kickPlayer = ApiBuilder.kickPlayer(sessionId, gameID.toString(), pid.toString(), reason)
        if (kickPlayer.reqBody.contains("Error", true)) {
            loger.error("在服务器{}踢出{}玩家失败 {}", gameID, pid, kickPlayer.reqBody)
        } else {
            loger.info("在服务器{}踢出{}玩家成功,理由:{}", gameID, pid, reason)
        }
    }
}