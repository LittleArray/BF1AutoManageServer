package instance

import api.ApiCore
import api.BtrApi
import api.GatewayApi
import com.google.gson.Gson
import data.*
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utils.ChineseTR.toTradition
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
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val loger: Logger = LoggerFactory.getLogger(this.javaClass)
    val pid: Long = p.PID
    var _p: PLBy22.ROST = p
        set(value) {
            val old = field
            if (old.JGTS != value.JGTS) {//加入时间变更了
                loger.info("玩家{}进服时间变更 {} -> {}", value.NAME, old.JGTS, value.JGTS)
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
    private var isExit = false
    private var isKick = false
    private var kickTimes = 0
    private var kickRes = ""
    private var kicKCD = 0
    var nextEnterTime = 0L
    var map = ""
    var isChangeMap = false
        set(value) {
            if (value) {
                changeMap()
            }
            field = value
        }
    var lkd: Double? = null
    var lkp: Double? = null
    var rkp: Double? = null
    var rkd: Double? = null
    var baseInfo: PlayerBaseInfo? = null

    init {
        init()
    }

    fun init() {
        loger.info("新玩家{}进入服务器{}", _p.NAME, serverSetting.get().gameId)
        isExit = false
        if (serverSetting.get().onlyBFEAC) return
        if (serverSetting.get().botlist.any { wl -> wl == _p.NAME }) return
        if (serverSetting.get().vbanlist.any { wl -> wl == _p.NAME }) {
            kick("VBan")
            return
        }
        if (nextEnterTime > 0) {
            if (System.currentTimeMillis() < nextEnterTime) {
                kick("Wait ${(nextEnterTime - System.currentTimeMillis()) / 1000}sec To Re-enter")
            } else {
                nextEnterTime = 0
            }
        }
        if (serverSetting.get().spectatorKick && _p.ROLE == "") {
            kick("NO WATCHING")
        }
        coroutineScope.launch {
            getBaseInfo()
            kickByTooManyBan()
            kickAlwaysByStats()
            kickAlwaysByClassRank()
            kickByPlatoons()
            kickByBtr()
        }
    }
    private suspend fun kickByLan(){
        if (serverSetting.get().maxPing != 0)
            if ((_p.PATT?.latency?:"0").toInt() > serverSetting.get().maxPing)
                kick("Lag ${_p.PATT?.latency}ms")
    }
    private suspend fun getBaseInfo(times: Int = 0) {
        baseInfo = ApiCore.getBaseInfo(pid.toString(), "false")
        if (baseInfo == null && times < 5) {
            loger.error("请求玩家{}生涯失败,5s后重新查询", _p.NAME)
            delay(5000)
            getBaseInfo(times + 1)
        }
    }

    private fun changeMap() {
        coroutineScope.launch {
            kickByBtr()
        }
    }

    private suspend fun kickByTooManyBan() {
        if (isExit) return
        if ((ApiCore.getBan(pid.toString())?.size ?: 0) > serverSetting.get().tooManyBan) {
            kick("Too Many Ban")
        }
    }

    private suspend fun kickByBtr(times: Int = 0) {
        if (isExit) return
        if (times > 5) return
        if (serverSetting.get().recMaxKD > 5.0 || serverSetting.get().recMaxKPM > 5.0 || serverSetting.get().recPlayTime > 30) return
        val btrMatches = BtrApi.recentlyServerSearch(_p.NAME, serverSetting.get().recCount)
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
                if (it?.isWinner == true) winTeam = it.id ?: 0.0
            }
            //val matchTime = it.data?.metadata?.timestamp?.replace("T", " ")?.replace("+00:00", "") ?: "1970-01-01 00:00:00"
            //2023-11-07T01:35:25+00:00
            run p@{
                it.data?.segments?.forEach {
                    if (it?.attributes?.playerId?.toLong() == pid && it.type == "player") {
                        time = it.stats?.time?.value?.div(60) ?: 0.0
                        kills = (it.stats?.kills?.value ?: 0).toInt()
                        deaths = (it.stats?.deaths?.value ?: 0).toInt()
                        kpm = it.stats?.killsPerMinute?.value ?: 0.0
                        isWin = winTeam == it.attributes.teamId
                        return@p
                    }
                }
            }
            // loger.info("{} RKD{} RKP{}",_p.NAME,kills.toDouble() / deaths.toDouble(),kpm)
            if (kpm > 0 && kills > 0) {
                rkp = rkp ?: kpm
                rkd = rkd ?: (kills.toDouble() / deaths.toDouble())
            }
            if (time > serverSetting.get().recPlayTime || kills > serverSetting.get().matchKillsEnable) {
                if ((kills.toDouble() / deaths.toDouble()) > serverSetting.get().recMaxKD) {
                    kick("Recently KD Limited ${serverSetting.get().recMaxKD}")
                }
                if ((kpm) > serverSetting.get().recMaxKPM) {
                    kick("Recently KPM Limited ${serverSetting.get().recMaxKPM}")
                }
            }
        }
        System.gc()
    }

    private suspend fun kickAlwaysByStats() {
        if (isExit) return
        if (baseInfo == null) {
            delay(5000)
            kickAlwaysByStats()
            return
        }
        val info = baseInfo!!
        val winPercent = info.wins.toDouble() / (info.wins + info.losses)
        val time = info.timePlayed.toDouble() / 60.0 / 60.0
        val kpm = info.kpm
        val kd = info.kd
        val rank = info.rank
        lkd = lkd ?: kd
        lkp = lkp ?: kpm
        if (kpm > serverSetting.get().lifeMaxKPM)
            kick("LifeKPM Limited ${serverSetting.get().lifeMaxKD}")
        if (kd > serverSetting.get().lifeMaxKD)
            kick("LifeKD Limited ${serverSetting.get().lifeMaxKD}")
        if (winPercent > serverSetting.get().winPercentLimited && rank > 65)
            kick("WinPercent Limited ${serverSetting.get().winPercentLimited * 100}%")
        if (rank > serverSetting.get().rankLimited)
            kick("Rank Limited ${serverSetting.get().rankLimited}")
        //低等级严管 45小时以下
        if (time < 45 && serverSetting.get().whitelist.none { it == _p.NAME } && serverSetting.get().lowRankMan) {//whitelist白名单,就是正常或者待观察的
            if (info.kills > 100) {
                if (info.accuracy > 0.5) {
                    loger.warn("[{}]{}低等级玩家数据异常生涯准确率>50%", rank, _p.NAME)
                    kick("Low Rank Life ACC Anomaly")
                }
                if ((info.headShots.toDouble() / info.kills.toDouble()) > 0.5) {
                    loger.warn("[{}]{}低等级玩家数据异常生涯爆头率>50%", rank, _p.NAME)
                    kick("Low Rank Life HS Anomaly")
                }
            }
            val weapons = ApiCore.getWeapons(pid.toString())
            weapons?.data?.forEach {
                val acc = it.accuracy ?: 0.0//后端处理过了
                val kills = (it.kills ?: 0)
                var vp = (it.hits?.toDouble()?.div(kills.toDouble()) ?: 999.0)
                vp = if (vp == 0.0) 999.0 else vp
                val wkpm = (it.kills?.toDouble() ?: 0.0) / (it.seconds?.div(60.0) ?: 0.0)
                val hs = try {
                    (it.headshots?.toDouble() ?: 0.0) / (it.kills?.toDouble() ?: 0.0)
                } catch (e: Exception) {
                    0.0
                }
                if (kills > 50) {
                    if (it.type != "霰彈槍" && it.type != "配備" && it.type != "近戰武器" && it.type != "手榴彈") {
                        if (acc > 50) {//武器准确率大于50%
                            loger.warn(
                                "[{}]{}低等级玩家数据异常,疑似锁腰子 {} 准确率>50%且武器击杀>50",
                                rank,
                                _p.NAME,
                                it.name
                            )
                            kick("Low Rank Weapon Anomaly")
                        }
                        if (hs > 0.5 && wkpm > 1) {//爆头率>50%且kpm>1且武器击杀>50
                            loger.warn(
                                "[{}]{}低等级玩家数据异常,爆头异常 {} 爆头率>50%且kpm>1且武器击杀>50",
                                rank,
                                _p.NAME,
                                it.name
                            )
                            kick("Low Rank Weapon Anomaly")
                        }
                        if (hs < 0.01) {
                            loger.warn(
                                "[{}]{}低等级玩家数据异常,疑似锁腰子 {} 爆头率<1%且武器击杀>50",
                                rank,
                                _p.NAME,
                                it.name
                            )
                            kick("Low Rank Weapon Anomaly")
                        }
                    }
                    if (it.type == "霰彈槍") {
                        if (acc > 100) {//霰弹枪命中率>100%
                            loger.warn(
                                "[{}]{}低等级玩家数据异常,喷子命中异常 {} 霰弹枪命中率>100%且武器击杀>50",
                                rank,
                                _p.NAME,
                                it.name
                            )
                            kick("Low Rank Weapon Anomaly")
                        }
                    }
                    //效率异常 效率指几颗子弹杀一个人,通常用来鉴别改伤
                    if (it.type == "輕機槍") {
                        if (vp < 3) {//3发子弹打死人
                            loger.warn(
                                "[{}]{}低等级玩家数据异常,机枪效率异常 {} 3发子弹打死人且武器击杀>50",
                                rank,
                                _p.NAME,
                                it.name
                            )
                            kick("Low Rank Weapon Anomaly")
                        }
                    }
                }
            }
        }
    }

    private suspend fun kickAlwaysByClassRank() {
        if (isExit) return
        if (serverSetting.get().classRankLimited.all { it.value > 50 }) return
        if (baseInfo == null) {
            delay(5000)
            kickAlwaysByStats()
            return
        }
        baseInfo?.classes?.forEach {
            when (it.className) {
                "突擊兵" -> {
                    val assault = serverSetting.get().classRankLimited["assault"]
                    if (it.classRank > (assault ?: 51))
                        kick("AssaultRank Limited $assault")
                }

                "偵察兵" -> {
                    val scout = serverSetting.get().classRankLimited["scout"]
                    if (it.classRank > (scout ?: 51))
                        kick("ScoutRank Limited $scout")
                }

                "醫護兵" -> {
                    val medic = serverSetting.get().classRankLimited["medic"]
                    if (it.classRank > (medic ?: 51))
                        kick("MedicRank Limited $medic")
                }

                "支援兵" -> {
                    val support = serverSetting.get().classRankLimited["support"]
                    if (it.classRank > (support ?: 51))
                        kick("SupportRank Limited $support")
                }

                "坦克" -> {
                    val tanker = serverSetting.get().classRankLimited["tanker"]
                    if (map != "MP_Argonne" && map != "MP_Forest" && map != "MP_Verdun" && map != "MP_Underworld" && map != "MP_Ridge")
                        if (it.classRank > (tanker ?: 51))
                            kick("TankerRank Limited $tanker")

                }

                "騎兵" -> {
                    val cavalry = serverSetting.get().classRankLimited["cavalry"]
                    if (map != "MP_Argonne" && map != "MP_Forest" && map != "MP_Verdun" && map != "MP_Underworld" && map != "MP_Ridge")
                        if (it.classRank > (cavalry ?: 51))
                            kick("CavalryRank Limited $cavalry")

                }

                "駕駛員" -> {
                    val piolt = serverSetting.get().classRankLimited["pilot"]
                    if (map != "MP_Argonne" && map != "MP_Forest" && map != "MP_Verdun" && map != "MP_Underworld" && map != "MP_Ridge")
                        if (it.classRank > (piolt ?: 51))
                            kick("PilotRank Limited $piolt")

                }
            }
        }
    }

    private suspend fun kickByPlatoons() {
        if (isExit) return
        if (serverSetting.get().platoonLimited.isEmpty()) return
        if (baseInfo == null) {
            delay(5000)
            kickAlwaysByStats()
            return
        }
        if (baseInfo!!.platoons.any { pp -> serverSetting.get().platoonLimited.any { it == pp.name } })
            kick("Platoon Limited")
    }

    fun update(p: PLBy22.ROST) {
        _p = p
        coroutineScope.launch {
            kickByLan()
        }
        if (isKick) {
            if (kickTimes < 15) {
                kick(kickRes, kicKCD)
            } else {
                loger.error("连续在服务器{}踢出玩家{}失败,已禁用踢出该玩家", serverSetting.get().gameId, _p.NAME)
                isKick = false
            }
            kickTimes++
        }
        //serverSetting = setting
    }

    fun exit() {
        loger.info("玩家{}离开服务器{}", _p.NAME, serverSetting.get().gameId)
        if (serverSetting.get().reEnterKick && nextEnterTime != 0L)
            nextEnterTime = System.currentTimeMillis() + 60 * 1000
        isExit = true
    }

    fun kick(reason: String = "Kick Without Reason", kickCD: Int = serverSetting.get().kickCD) {
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
                reason.toTradition()
            )
        } else {
            loger.info("在服务器{}踢出玩家{}成功,理由:{}", serverSetting.get().gameId.toString(), _p.NAME, reason)
            if (kickCD > 0) {
                kicKCD = kickCD
                nextEnterTime = System.currentTimeMillis() + (kickCD * 60 * 1000)
            }
        }
    }

}