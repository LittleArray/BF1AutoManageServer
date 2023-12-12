package instance

import api.ApiCore
import api.BtrApi
import api.GatewayApi
import data.PLBy22
import data.PlayerBaseInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utils.ChineseTR.toTradition
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
                    loger.info("玩家{}队伍变更 {} ->  {} ", value.NAME, old.TEAMNAME, value.TEAMNAME)
                } else {
                    isChangeMap = false
                }
            }
            if (old.PATT?.latency != value.PATT?.latency){
                kickByLan()
            }
            field = value
        }
    private var kickRes = ""
    private var KickCD: Int? = null
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
    var rtime: Double? = null
    var baseInfo: PlayerBaseInfo? = null

    init {
        init()
    }

    fun init() {
        loger.info("新玩家 {} 进入服务器 {} ", _p.NAME, serverSetting.get().gameId)
        if (serverSetting.get().onlyBFEAC) return
        if (serverSetting.get().botlist.any { wl -> wl == _p.NAME }) return
        if (config.Config.Config.botlist.any { wl -> wl == _p.NAME }) return
        if (serverSetting.get().killsMax > 0) {
            if ((serverSetting.get().killsMap[pid.toString()] ?: 0) > serverSetting.get().killsMax) {
                kick("请联系管理员")
                return
            }
        }
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

    private fun kickByLan() {
        if (serverSetting.get().maxPing != 0)
            if ((_p.PATT?.latency ?: "0").toInt() > serverSetting.get().maxPing)
                kick("Lag ${_p.PATT?.latency}ms")
    }

    private suspend fun getBaseInfo(times: Int = 0) {
        baseInfo = ApiCore.getBaseInfo(pid.toString(), "false")
        if (baseInfo == null && times < 5) {
            loger.error("请求玩家 {} 生涯失败,5s后重新查询", _p.NAME)
            delay(5000)
            getBaseInfo(times + 1)
        }
    }

    private fun changeMap() {
        coroutineScope.launch {
            kickByBtr()
        }
    }

    private fun kickByTooManyBan() {
        if ((ApiCore.getBan(pid.toString())?.size ?: 0) > serverSetting.get().tooManyBan) {
            kick("Too Many Ban")
        }
    }

    private fun kickByBtr(times: Int = 0) {
        if (times > 5) return
        if (serverSetting.get().recMaxKD > 5.0 && serverSetting.get().recMaxKPM > 5.0 && serverSetting.get().recPlayTime > 30) return
        val btrMatches = BtrApi.recentlyServerSearch(_p.NAME, pid.toString(), serverSetting.get().recCount)
        var time = 0.0
        var kills = 0
        var deaths = 0
        btrMatches.forEach {
            //最近3小时
            //loger.info(" {}   {} ",System.currentTimeMillis(),MatchTime.time + 1000 * 60 * 60 * 8 + (3 * 60 * 60 * 1000))
            val ntime = it.stats?.time?.value?.div(60) ?: 0.0
            val nkills = (it.stats?.kills?.value?.toInt() ?: 0) + (it.stats?.killsAssistAsKills?.value?.toInt() ?:0)
            val ndeaths = it.stats?.deaths?.value?.toInt() ?: 0
            time += ntime
            kills += nkills
            deaths += ndeaths
        }
        val kpm = kills / time
        val kd = kills.toDouble() / deaths.toDouble()
        loger.debug(" {}  Kills: {}  RKD: {}  RKP: {}  Time: {} ", _p.NAME, kills, kd, kpm, time)
        if (kpm > 0 && kd > 0) {
            rkp = rkp ?: kpm
            rkd = rkd ?: kd
            rtime = time
        }
        if (kills > serverSetting.get().matchKillsEnable) {
            if (kd > serverSetting.get().recMaxKD) {
                kick("Recently KD Limited ${serverSetting.get().recMaxKD}")
            }
            if (kpm > serverSetting.get().recMaxKPM) {
                kick("Recently KPM Limited ${serverSetting.get().recMaxKPM}")
            }
        }
    }

    private suspend fun kickAlwaysByStats() {
        if (baseInfo == null) {
            delay(5000)
            kickAlwaysByStats()
            return
        }
        val info = baseInfo!!
        val winPercent = info.wins.toDouble() / (info.wins + info.losses)
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
        LowRankChecker(info, this, serverSetting).check()
    }

    private suspend fun kickAlwaysByClassRank() {
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
            if (kickRes.isNotEmpty()) {
                kick(kickRes, KickCD ?: serverSetting.get().kickCD)
            }
        }
        //serverSetting = setting
    }

    fun exit() {
        loger.info("玩家 {} 离开服务器 {} ", _p.NAME, serverSetting.get().gameId)
        if (whitelist()) return
        if (serverSetting.get().killsMax > 0) {
            coroutineScope.launch {
                delay(3 * 60 * 1000)
                val btrMatches = BtrApi.recentlyServerSearch(_p.NAME, pid.toString(), 1.0)
                btrMatches.firstOrNull()?.let {
                    var kills = serverSetting.get().killsMap[pid.toString()] ?: 0
                    kills += (it.stats?.kills?.value?.toInt() ?: 0) + (it.stats?.killsAssistAsKills?.value?.toInt() ?:0)
                    if (kills > 0) serverSetting.get().killsMap[pid.toString()] = kills
                }
            }
        }
        if (serverSetting.get().reEnterKick)
            nextEnterTime = System.currentTimeMillis() + 60 * 1000
    }
    fun whitelist():Boolean{
        if (serverSetting.get().whitelist.any { wl -> wl == _p.NAME }) return true
        if (serverSetting.get().adminlist.any { wl -> wl == _p.PID.toString() }) return true
        if (serverSetting.get().botlist.any { wl -> wl == _p.PID.toString() }) return true
        if (config.Config.Config.botlist.any { wl -> wl == _p.NAME }) return true
        return false
    }
    fun kick(reason: String = "Kick Without Reason", kickCD: Int = serverSetting.get().kickCD, times: Int = 0) {
        if (whitelist()) return
        kickRes = reason
        KickCD = kickCD
        val kickPlayer = GatewayApi.kickPlayer(sessionId, serverSetting.get().gameId.toString(), pid.toString(), reason)
        if (kickPlayer.reqBody.contains("Error", true)) {
            loger.error(
                "在服务器 {} 踢出玩家 {} 失败,理由: {} ",
                serverSetting.get().gameId.toString(),
                _p.NAME,
                reason.toTradition()
            )
            if (times < 5)
                kick(reason, kickCD, times + 1)
            else
                loger.error("多次踢出玩家无效,已禁止重复踢出该玩家  {} ", _p.NAME)
        } else {
            loger.info(
                "在服务器 {} 踢出玩家 {} 成功,理由: {} ",
                serverSetting.get().gameId.toString(),
                _p.NAME,
                reason.toTradition()
            )
            if (kickCD > 0) {
                nextEnterTime = System.currentTimeMillis() + (kickCD * 60 * 1000)
            }
        }
    }

}