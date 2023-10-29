package instance

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
    map: String,
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
                if (serverSetting.get().spectatorKick && value.ROLE == ""){
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
    var mapPretty: String = map
        set(value) {
            val old = field
            if (old != value) {
                //地图更新
                isChangeMap = true
                coroutineScope.launch {
                    updateMatch()
                    randomWpCheck(true)
                    randomVpCheck(true)
                }
            }
            field = value
        }
    var teamName: String = ""
    var isExit = false
    var isChangeMap = false
    var nextEnterTime = 0L
    var oldKills = 0
    var oldDeath= 0

    init {
        init()
    }

    fun init() {
        loger.info("新玩家{}进入服务器{}", _p.NAME, serverSetting.get().gameId)
        isExit = false
        if (serverSetting.get().whitelist.any { wl -> wl == _p.NAME }) return
        if (serverSetting.get().botlist.any { wl -> wl == _p.NAME }) return
        if (nextEnterTime > 0) {
            if (System.currentTimeMillis() < nextEnterTime) {
                kick("You Can Re-enter After ${SimpleDateFormat("HH:mm").format(nextEnterTime)}")
            } else {
                nextEnterTime = 0
            }
        }
        if (serverSetting.get().spectatorKick && _p.ROLE == ""){
            kick("NO WATCHING")
        }
        coroutineScope.launch {
            kickAlwaysByStats()
            delay((Math.random() * 1000).toLong())
            kickAlwaysByClassRank()
            delay((Math.random() * 1000).toLong())
            randomWpCheck()
            delay((Math.random() * 1000).toLong())
            randomVpCheck()
        }
    }

    suspend fun randomWpCheck(isAlwaysCheck: Boolean = false, times: Int = 0) {
        if (isExit) return
        //loger.info("随机查询{}武器",_p.NAME)
        if (serverSetting.get().weaponLimited.isEmpty() && serverSetting.get().weaponStarLimited > 101) return
        val weapons = getWeapons()
        weapons?.result?.forEach { wp ->
            wp.weapons.forEach {
                val wps = it.stats.values
                val name = it.name
                val kickName = if (name.length > 12) name.subSequence(0, 12) else name
                val kills = wps?.kills ?: 0
                val shots = wps?.shots ?: kills
                if (serverSetting.get().weaponLimited.any { wpl -> wpl == name }) {
                    //loger.info("{}受限武器{}记录,当前击杀{},开火次数{}",_p.NAME, name,wps?.kills,wps?.shots)
                    if (shots > (KitCache.cache[name]?.get(pid) ?: (shots + 1))) {
                        kick("Ban $kickName", 15)
                    }
                }
                if ((wps?.kills?.div(100) ?: 0) > serverSetting.get().weaponStarLimited) {
                    //loger.info("{}的{}大于{}星,当前击杀{},开火次数{}",_p.NAME, name,serverSetting.get().weaponStarLimited,wps?.kills,wps?.shots)
                    if (shots > (KitCache.cache[name]?.get(pid) ?: (shots + 1))) {
                        kick("$kickName Over ${serverSetting.get().weaponStarLimited} Stars", 15)
                    }
                }
                if (KitCache.cache[name].isNullOrEmpty()) {
                    KitCache.cache[name] = mutableMapOf()
                    KitCache.cache[name]?.put(pid, shots)
                } else {
                    KitCache.cache[name]?.put(pid, shots)
                }
            }
        }
        if (weapons != null) {
            return
        } else {
            loger.error("请求玩家{}武器数据失败,60s后重新查询", _p.NAME)
            delay(60 * 1000)
            randomWpCheck()
        }
        if (isAlwaysCheck && times < 5) {
            loger.error("请求玩家{}武器数据失败,60s后重新查询", _p.NAME)
            delay(60 * 1000)
            randomWpCheck(true, times + 1)
        }
    }

    suspend fun randomVpCheck(isAlwaysCheck: Boolean = false, times: Int = 0) {
        if (isExit) return
        if (serverSetting.get().vehicleLimited.isEmpty() && serverSetting.get().vehicleStarLimited > 101) return
        //loger.info("随机查询{}武器",_p.NAME)
        val vehicles = getVehicles()
        vehicles?.result?.forEach { wp ->
            wp.vehicles.forEach {
                val vpn = it.stats.values
                val name = it.name
                val kickName = if (name.length > 12) name.subSequence(0, 12) else name
                val kills:Int= (vpn?.kills?:0).toInt()
                if (serverSetting.get().vehicleLimited.any { wpl -> wpl == name }) {
                    //loger.info("{}受限武器{}记录,当前击杀{},开火次数{}",_p.NAME, name,wps?.kills,wps?.shots)
                    if (kills > (KitCache.cache[name]?.get(pid)?:(kills+1))) {
                        kick("Ban $kickName", 15)
                    }
                }
                if (kills / 100 > serverSetting.get().vehicleStarLimited) {
                    //loger.info("{}的{}大于{}星,当前击杀{},开火次数{}",_p.NAME, name,serverSetting.get().weaponStarLimited,wps?.kills,wps?.shots)
                    if (kills > (KitCache.cache[name]?.get(pid) ?: (kills + 1))) {
                        kick("$kickName Over ${serverSetting.get().vehicleStarLimited} Stars", 15)
                    }
                }
                if (KitCache.cache[name].isNullOrEmpty()) {
                    KitCache.cache[name] = mutableMapOf()
                    KitCache.cache[name]?.put(pid, kills)
                } else {
                    KitCache.cache[name]?.put(pid, kills)
                }
            }
        }
        if (vehicles != null) {
            return
        } else {
            loger.error("请求玩家{}载具数据失败,60s后重新查询", _p.NAME)
            delay(60 * 1000)
            randomVpCheck()
        }
        if (isAlwaysCheck && times < 5) {
            loger.error("请求玩家{}载具数据失败,60s后重新查询", _p.NAME)
            delay(60 * 1000)
            randomVpCheck(true, times + 1)
        }
    }

    private suspend fun updateMatch(){
        if (isExit) return
        val stats = getStats()
        if (stats != null) {
            var nowKills = stats.result.basicStats.kills
            var nowDeath = stats.result.basicStats.deaths
            if (nowKills > oldKills){
                val kills = nowKills - oldKills
                val death = nowDeath - oldDeath
                val kd = kills.toDouble() / death.toDouble()
                if (kills > 0 && oldKills != 0 && oldDeath != 0)  {
                    loger.info("服务器{}对局结算 玩家{} 击杀{} 死亡{} KD{}",serverSetting.get().gameId,_p.NAME, kills, death,kd)
                    if (kills > serverSetting.get().matchKillsEnable){
                        if (kills > serverSetting.get().killsLimited){
                            kick("Kills Limited ${serverSetting.get().killsLimited}",15)
                        }
                        if (kd > serverSetting.get().matchKDLimited){
                            kick("KD Limited ${serverSetting.get().matchKDLimited}",15)
                        }
                    }
                }
            }else{
                delay(60 * 1000)
                updateMatch()
            }
        }
    }

    private suspend fun kickAlwaysByStats() {
        if (isExit) return
        val stats = getStats()
        if (stats != null) {
            oldKills = stats.result.basicStats.kills
            oldDeath = stats.result.basicStats.deaths
            //loger.info("玩家{}生涯数据:KD:{}", _p.NAME, stats.result.kdr)
            val winPercent =
                stats.result.basicStats.wins.toDouble() / (stats.result.basicStats.wins + stats.result.basicStats.losses)
            val rank = ExperienceConversion.toRank(stats.result.basicStats.spm, stats.result.basicStats.timePlayed)
            if (stats.result.basicStats.kpm > serverSetting.get().lifeMaxKPM)
                kick("LifeKPM Limited ${serverSetting.get().lifeMaxKD}")
            if (stats.result.kdr > serverSetting.get().lifeMaxKD)
                kick("LifeKD Limited ${serverSetting.get().lifeMaxKD}")
            if (winPercent > serverSetting.get().winPercentLimited && rank > 25)
                kick("WinPercent Limited ${serverSetting.get().winPercentLimited * 100}%")
            if (rank > serverSetting.get().rankLimited)
                kick("Rank Limited ${serverSetting.get().rankLimited}")
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

    fun update(p: PLBy22.ROST, map: String) {
        mapPretty = map
        _p = p
        //serverSetting = setting
    }

    fun exit() {
        loger.info("玩家{}离开服务器{}", _p.NAME, serverSetting.get().gameId)
        KitCache.cache.forEach { name, pidd ->
            pidd.remove(pid)
        }
        isExit = true
    }

    fun kick(reason: String = "kick without reason", kickCD: Int = serverSetting.get().kickCD) {
        val kickPlayer = GatewayApi.kickPlayer(sessionId, serverSetting.get().gameId.toString(), pid.toString(), reason)
        if (kickPlayer.reqBody.contains("Error", true)) {
            loger.error(
                "在服务器{}踢出玩家{}失败",
                serverSetting.get().gameId.toString(),
                _p.NAME
            )
            coroutineScope.launch {
                delay(60*1000)
                kick(reason, kickCD)
            }
        } else {
            loger.info("在服务器{}踢出玩家{}成功,理由:{}", serverSetting.get().gameId.toString(), _p.NAME, reason)
            if (kickCD > 0) nextEnterTime = System.currentTimeMillis() + (kickCD * 60 * 1000)
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


}