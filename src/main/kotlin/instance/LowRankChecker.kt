package instance

import api.ApiCore
import api.BFEACApi
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LowRankChecker(private val player: Player) {

    val serverSetting = player.serverSetting
    private val loger: Logger = LoggerFactory.getLogger(this.javaClass)


    init {
        runBlocking {
            if (player.baseInfo == null)
                player.getBaseInfo()
        }
        //loger.debug("准备对低等级玩家进行检测 PID:{} ID:{}", player._p.PID, id)
    }

    fun check() {
        val info = player.baseInfo!!
        val time = info.timePlayed.toDouble() / 60.0 / 60.0
        val rank = info.rank
        val id = player._p.NAME
        var diff = ""
        var kick = false
        var eacS = false
        //低等级严管 65小时以下
        if (info.rank > 0 && time < 65 && !player.whitelist() && serverSetting.get().lowRankMan) {//whitelist白名单,就是正常或者待观察的
            if (info.kills > 100) {
                if (info.accuracy > 0.5) {
                    diff += "异常生涯准确率>50%&&"
                }
                if ((info.headShots.toDouble() / info.kills.toDouble()) > 0.5) {
                    diff += "异常生涯爆头率>50%&&"
                }
            }
            run p@{
                val weapons = ApiCore.getWeapons(player.pid.toString())
                weapons?.data?.let { data ->
                    for (it in data) {
                        val acc = it.accuracy ?: 0.0//后端处理过了
                        val kills = (it.kills ?: 0)
                        var vp = (it.hits?.toDouble()?.div(kills.toDouble()) ?: 999.0)
                        vp = if (vp == 0.0) 999.0 else vp
                        val wkpm = (it.kills?.toDouble() ?: 0.0) / (it.seconds?.div(60.0) ?: 0.0)
                        val wtime = (it.seconds?:1.0) / 60
                        val hs = try {
                            (it.headshots?.toDouble() ?: 0.0) / (it.kills?.toDouble() ?: 0.0)
                        } catch (e: Exception) {
                            0.0
                        }
                        if (kills == 300) continue
                        when((it.headshots?.toDouble() ?: 0.0)){
                            3.0 -> continue
                            20.0 -> continue
                        }
                        if (diff != "" && !eacS) {
                            eacS = true
                            val eacState = BFEACApi.getEacState(id)
                            if (eacState?.error_code == 0) {
                                loger.info(
                                    "BFEAC存在此玩家 {} 的案件 https://www.bfeac.com/#/case/{} 已跳出检查",
                                    id,
                                    eacState.data?.firstOrNull()?.case_id
                                )
                                if ((eacState.data?.firstOrNull()?.current_status?:0) > 1) return
                                diff += "请注意BFEAC存在此玩家 ${id} 的案件 https://www.bfeac.com/#/case/${eacState.data?.firstOrNull()?.case_id} &&"
                                kick = true
                                player.kick("Ban By LRC Strategy")
                                ServerInstance.INSTANCE.forEach {
                                    if (it.serverSetting.gameId == player.serverSetting.get().gameId){
                                        it.lrcLog = Server.LRCLog(id,System.currentTimeMillis(),time.toLong(),kick,diff)
                                    }
                                }
                                return
                            }
                        }
                        var task = ""
                        WeaposTask.hm.forEach { (wpName, k) ->
                            if (it.name?.contains(wpName) == true) {
                                task = ",可能刷枪用于解锁 ${k.replace(" "," 条件 ")}"
                            }
                        }
                        val wpDetail = "${it.name} Kills:${kills} KPM: ${String.format("%.2f", wkpm)} ACC: ${String.format("%.2f", acc)} HS: ${String.format("%.2f", hs * 100)} 效率: ${String.format("%.2f", vp)} ${task}"
                        if (kills > 60 && wtime > 20) {//武器击杀大于60
                            //////////////////////通用判定//////////////////////
                            if (it.type != "霰彈槍" && it.type != "配備" && it.type != "近戰武器" && it.type != "手榴彈" && it.type != "戰場裝備" && it.type != "佩槍") {
                                if (wkpm > info.kpm * 4 && it.name?.contains("1895") != true) {//与生涯KPM差异过大
                                    diff += "疑似改伤 $wpDetail &&"
                                }
                                if (acc > 70 && wkpm > 1.8) {//准确率大于50%且kpm>1
                                    diff += "疑似锁腰子 $wpDetail &&"
                                }
                                if (hs > 0.6 && wkpm > 1.8 || hs > 0.85) {//爆头率>60%且kpm>1.8或爆头率>85%
                                    diff += "爆头异常 $wpDetail &&"
                                }
                                if (hs < 0.01) {
                                    diff += "疑似锁腰子 $wpDetail &&"
                                }
                            }

                            //////////////////////霰弹枪//////////////////////
                            if (it.type == "霰彈槍") {
                                if (acc > 100) {//霰弹枪命中率>100%
                                    diff += "喷子命中异常 $wpDetail &&"
                                    kick = true
                                }
                                if (it.name?.contains("霰彈塊") != true && hs > 0.4) {
                                    diff += "喷子爆頭异常 $wpDetail &&"
                                    kick = true
                                }
                            }
                            //////////////////////冲锋枪//////////////////////
                            if (it.type == "衝鋒槍") {
                                if (hs > 0.25 && wkpm > 1.8 || hs > 0.35) {
                                    diff += "冲锋枪爆头异常 $wpDetail &&"
                                    kick = true
                                }
                            }
                            //////////////////////步槍(狙)//////////////////////
                            if (it.type == "步槍") {
                                if (vp < 1.1 && wkpm > 1.8 && it.name?.contains("1895（步兵）") == false) {
                                    diff += "效率异常 $wpDetail &&"
                                    kick = true
                                    continue
                                }
                            }
                            //////////////////////通用效率检测////////////////////
                            //冲锋枪 机枪 半自动 别的不判
                            EfficiencyMap.hm.forEach { (wpName, k) ->
                                if (it.name?.contains(wpName) == true) {
                                    val kV = 100.0 / k
                                    if (vp < kV - 0.2 && wkpm > 1.8) {
                                        diff += "效率异常 $wpDetail &&"
                                    }
                                }
                            }
                        }
                    }
                }

            }
            ////////////////////总结/////////////////////
            if (diff.isEmpty()) return
            val diffs = diff.split("&&").filter { it.isNotEmpty() }
            diffs.forEach {
                loger.warn("[{}h] {} PID: {} {}", time.toInt(), id, player._p.PID, it)
            }
            if (diffs.size > 1) kick = true
            if (!serverSetting.get().lrcKick) kick = false
            if (kick) {
                loger.info("经过判定踢出玩家 {}", id)
                player.kick("Low Rank Weapon Anomaly")
            }
            if (diffs.isNotEmpty()){
                ServerInstance.INSTANCE.forEach {
                    if (it.serverSetting.gameId == player.serverSetting.get().gameId){
                        it.lrcLog = Server.LRCLog(id,System.currentTimeMillis(),time.toLong(),kick,diff)
                    }
                }
            }
        }
    }

}