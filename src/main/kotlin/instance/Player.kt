package instance

import api.BtrApi
import api.GatewayApi
import com.google.gson.Gson
import data.*
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import kotlin.reflect.KMutableProperty0

object ExperienceConversion {
    val exp = listOf(
        1000,
        5000,
        15000,
        25000,
        40000,
        55000,
        75000,
        95000,
        120000,
        145000,
        175000,
        205000,
        235000,
        265000,
        295000,
        325000,
        355000,
        395000,
        435000,
        475000,
        515000,
        555000,
        595000,
        635000,
        675000,
        715000,
        755000,
        795000,
        845000,
        895000,
        945000,
        995000,
        1045000,
        1095000,
        1145000,
        1195000,
        1245000,
        1295000,
        1345000,
        1405000,
        1465000,
        1525000,
        1585000,
        1645000,
        1705000,
        1765000,
        1825000,
        1885000,
        1945000,
        2015000,
        2085000,
        2155000,
        2225000,
        2295000,
        2365000,
        2435000,
        2505000,
        2575000,
        2645000,
        2745000,
        2845000,
        2945000,
        3045000,
        3145000,
        3245000,
        3345000,
        3445000,
        3545000,
        3645000,
        3750000,
        3870000,
        4000000,
        4140000,
        4290000,
        4450000,
        4630000,
        4830000,
        5040000,
        5260000,
        5510000,
        5780000,
        6070000,
        6390000,
        6730000,
        7110000,
        7510000,
        7960000,
        8430000,
        8960000,
        9520000,
        10130000,
        10800000,
        11530000,
        12310000,
        13170000,
        14090000,
        15100000,
        16190000,
        17380000,
        20000000,
        20500000,
        21000000,
        21500000,
        22000000,
        22500000,
        23000000,
        23500000,
        24000000,
        24500000,
        25000000,
        25500000,
        26000000,
        26500000,
        27000000,
        27500000,
        28000000,
        28500000,
        29000000,
        29500000,
        30000000,
        30500000,
        31000000,
        31500000,
        32000000,
        32500000,
        33000000,
        33500000,
        34000000,
        34500000,
        35000000,
        35500000,
        36000000,
        36500000,
        37000000,
        37500000,
        38000000,
        38500000,
        39000000,
        39500000,
        40000000,
        41000000,
        42000000,
        43000000,
        44000000,
        45000000,
        46000000,
        47000000,
        48000000,
        49000000,
        50000000
    )

    fun toRank(spm: Double, timePlay: Int): Int {
        var allExp: Long = spm.toLong() * (timePlay / 60)
        var rank = 0
        run p@{
            exp.forEach {
                //println(it)
                //println(allExp)
                if (allExp - it > 0) if (rank > 150) return@p else rank++ else return@p
            }
        }
        return rank
    }
}

/**
 * @Description
 * @Author littleArray
 * @Date 2023/10/7
 */
class Player(
    val sessionId: String,
    p: PLBy22.ROST,
    var serverSetting: KMutableProperty0<Server.ServerSetting>,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val loger: Logger = LoggerFactory.getLogger(this.javaClass)
    val pid: Long = p.PID
    var _p: PLBy22.ROST = p
        set(value) {
            val old = field
            if (old.JGTS != value.JGTS) {//加入时间变更了
                loger.info("玩家{}进服时间变更", value.NAME)
                exit()
                init()
            }
            if (old.ROLE != value.ROLE) {//身份变更
                loger.info("玩家{}身份变更 {}", value.NAME, value.ROLE)
                if (serverSetting.get().spectatorKick && value.ROLE == "") {
                    kick("NO WATCHING")
                }
            }
            if (old.TIDX != value.TIDX) {//队伍变更
                if (!isChangeMap) {
                    loger.info("玩家{}队伍变更 {} -> {}", value.NAME, old.TEAMNAME, value.TEAMNAME)
                } else {
                    isChangeMap = false
                }
            }
            field = value
        }
    var teamName: String = ""
    var isExit = false
    var isKick = false
    var kickRes = ""
    var kicKCD = 0
    var isChangeMap = false
        set(value) {
            if (value){
                changeMap()
                field = false
            }
        }
    var nextEnterTime = 0L
    var lkd :Double ?=null
    var lkp :Double ?=null
    var rkp :Double ?=null
    var rkd :Double ?=null

    init {
        init()
    }

    fun init() {
        loger.info("新玩家{}进入服务器{}", _p.NAME, serverSetting.get().gameId)
        isExit = false
        if (serverSetting.get().botlist.any { wl -> wl == _p.NAME }) return
        if (serverSetting.get().vbanlist.any { wl -> wl == _p.NAME }) {
            kick("VBan")
            return
        }
        if (nextEnterTime > 0) {
            if (System.currentTimeMillis() < nextEnterTime) {
                kick("Wait ${SimpleDateFormat("mm").format(nextEnterTime - System.currentTimeMillis())}mins To Re-enter")
            } else {
                nextEnterTime = 0
            }
        }
        if (serverSetting.get().spectatorKick && _p.ROLE == "") {
            kick("NO WATCHING")
        }
        coroutineScope.launch {
            kickAlwaysByStats()
            kickAlwaysByClassRank()
            kickByPlatoons()
            kickByBtr()
        }
    }
    private fun changeMap(){
        coroutineScope.launch {
            kickByBtr()
        }
    }
    private suspend fun kickByBtr(times: Int = 0){
        if (isExit) return
        if (times > 5) return
        if (serverSetting.get().recMaxKD > 5.0 && serverSetting.get().recMaxKPM > 5.0) return
        val btrMatches = BtrApi.recentlyServerSearch(_p.NAME)
        if (btrMatches == null) {
            delay(60 * 1000)
            kickByBtr(times + 1)
            return
        }
        btrMatches.forEach {
            var time = 0.0
            var kills = 0
            var deaths = 0
            var kpm = 0.0
            var winTeam = 0.0
            var isWin = false
            it.data?.metadata?.teams?.forEach {
                if (it?.isWinner == true) winTeam = it.id ?:0.0
            }
            val matchTime = it.data?.metadata?.timestamp?.replace("T"," ")?.replace("+00:00","")?:"1970-01-01 00:00:00"
            //2023-11-07T01:35:25+00:00
            run p@{
                it.data?.segments?.forEach {
                    if (it?.attributes?.playerId?.toLong() == pid && it.type == "player"){
                        time = it.stats?.time?.value?.div(60)?:0.0
                        kills = (it.stats?.kills?.value?:0).toInt()
                        deaths = (it.stats?.deaths?.value?:0).toInt()
                        kpm = it.stats?.killsPerMinute?.value?:0.0
                        isWin = winTeam == it.attributes.teamId
                        return@p
                    }
                }
            }
           // loger.info("{} RKD{} RKP{}",_p.NAME,kills.toDouble() / deaths.toDouble(),kpm)
            if (kpm > 0 && kills >0){
                rkp = rkp ?:kpm
                rkd = rkd ?:(kills.toDouble() / deaths.toDouble())
            }
            if (time > serverSetting.get().recPlayTime || kills > serverSetting.get().matchKillsEnable){
                if ((kills.toDouble() / deaths.toDouble()) > serverSetting.get().recMaxKD){
                    kick("Recently KD Limited ${serverSetting.get().recMaxKD}")
                }
                if ((kpm) > serverSetting.get().recMaxKPM){
                    kick("Recently KPM Limited ${serverSetting.get().recMaxKPM}")
                }
            }
        }
    }

    private suspend fun kickAlwaysByStats() {
        if (isExit) return
        val stats = getStats()
        if (stats != null) {
            //loger.info("玩家{}生涯数据:KD:{}", _p.NAME, stats.result.kdr)
            val kpm = stats.result.basicStats.kpm
            val kd = stats.result.kdr
            lkd = lkd?:kd
            lkp = lkp?:kpm
            val winPercent = stats.result.basicStats.wins.toDouble() / (stats.result.basicStats.wins + stats.result.basicStats.losses)
            val rank = ExperienceConversion.toRank(stats.result.basicStats.spm, stats.result.basicStats.timePlayed)
            if (kpm > serverSetting.get().lifeMaxKPM)
                kick("LifeKPM Limited ${serverSetting.get().lifeMaxKD}")
            if (kd > serverSetting.get().lifeMaxKD)
                kick("LifeKD Limited ${serverSetting.get().lifeMaxKD}")
            if (winPercent > serverSetting.get().winPercentLimited && rank > 25)
                kick("WinPercent Limited ${serverSetting.get().winPercentLimited * 100}%")
            if (rank > serverSetting.get().rankLimited)
                kick("Rank Limited ${serverSetting.get().rankLimited}")
            if (rank > 95){
                if (kd > serverSetting.get().lifeMaxKD95)
                    kick("Rank>95 KD Limited ${serverSetting.get().lifeMaxKD95}")
                if (kpm > serverSetting.get().lifeMaxKPM95)
                    kick("Rank>95 KPM Limited ${serverSetting.get().lifeMaxKPM95}")
            }
            if (rank >= 150){
                if (kd > serverSetting.get().lifeMaxKD150)
                    kick("Rank150 KD Limited ${serverSetting.get().lifeMaxKD150}")
                if (kpm > serverSetting.get().lifeMaxKPM150)
                    kick("Rank150 KPM Limited ${serverSetting.get().lifeMaxKPM150}")
            }
        } else {
            loger.error("请求玩家{}生涯失败,5s后重新查询", _p.NAME)
            delay(5000)
            kickAlwaysByStats()
        }
    }

    private suspend fun kickAlwaysByClassRank() {
        if (isExit) return
        if (serverSetting.get().classRankLimited.isEmpty()) return
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

    private suspend fun kickByPlatoons(){
        if (isExit) return
        if (serverSetting.get().platoonLimited.isEmpty()) return
        val platoons = getPlatoons()
        if (platoons != null){
            platoons.result.any { pp -> serverSetting.get().platoonLimited.any { it == pp.name } }
                kick("Platoon Limited")
        }
    }

    fun update(p: PLBy22.ROST) {
        _p = p
        if (isKick) kick(kickRes,kicKCD)
        //serverSetting = setting
    }

    fun exit() {
        loger.info("玩家{}离开服务器{}", _p.NAME, serverSetting.get().gameId)
        isExit = true
    }

    fun kick(reason: String = "kick without reason", kickCD: Int = serverSetting.get().kickCD, times: Int = 0) {
        if (serverSetting.get().whitelist.any { wl -> wl == _p.NAME }) return
        if (serverSetting.get().adminlist.any { wl -> wl == _p.PID.toString() }) return
        isKick = true
        kickRes = reason
        val kickPlayer = GatewayApi.kickPlayer(sessionId, serverSetting.get().gameId.toString(), pid.toString(), reason)
        if (kickPlayer.reqBody.contains("Error", true)) {
            loger.error(
                "在服务器{}踢出玩家{}失败,理由:{}",
                serverSetting.get().gameId.toString(),
                _p.NAME,
                reason
            )
            if (times > 3) return
            coroutineScope.launch {
                delay(60 * 1000)
                kick(reason, kickCD, times + 1)
            }
        } else {
            loger.info("在服务器{}踢出玩家{}成功,理由:{}", serverSetting.get().gameId.toString(), _p.NAME, reason)
            if (kickCD > 0) {
                kicKCD = kickCD
                nextEnterTime = System.currentTimeMillis() + (kickCD * 60 * 1000)
            }
        }
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
        val jsonRpcObj = JsonRpcObj(
            method = method,
            params = object {
                val game = "tunguska"
                val personaId = pid.toString()
            }
        )

        val body = Gson().toJson(jsonRpcObj)
        val builder = GatewayApi.jsonRpc(body, sessionId)
        if (builder.isSuccessful) {
            return try {
                val stats = Gson().fromJson(builder.reqBody, GatewayStats::class.java)
                stats
            } catch (e: Exception) {
                loger.error("获取{}玩家生涯数据失败,{}", pid, e.stackTraceToString())
                null
            }

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


}