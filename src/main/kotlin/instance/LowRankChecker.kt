package instance

import api.ApiCore
import data.PlayerBaseInfo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KMutableProperty0

class LowRankChecker(private var info: PlayerBaseInfo, private val player: Player, private val serverSetting: KMutableProperty0<Server.ServerSetting>) {
    private val loger: Logger = LoggerFactory.getLogger(this.javaClass)
    val winPercent = info.wins.toDouble() / (info.wins + info.losses)
    val time = info.timePlayed.toDouble() / 60.0 / 60.0
    val kpm = info.kpm
    val kd = info.kd
    val rank = info.rank

    fun check(){
        val plname = player._p.NAME
        //低等级严管 45小时以下
        if (time < 45 && serverSetting.get().whitelist.none { it == plname } && serverSetting.get().lowRankMan) {//whitelist白名单,就是正常或者待观察的
            if (info.kills > 100) {
                if (info.accuracy > 0.5) {
                    loger.warn("[{}]{}低等级玩家数据异常生涯准确率>50%", rank, plname)
                    player.kick("Low Rank Life ACC Anomaly")
                }
                if ((info.headShots.toDouble() / info.kills.toDouble()) > 0.5) {
                    loger.warn("[{}]{}低等级玩家数据异常生涯爆头率>50%", rank, plname)
                    player.kick("Low Rank Life HS Anomaly")
                }
            }
            val weapons = ApiCore.getWeapons(player.pid.toString())
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
                if (kills > 60) {//武器击杀大于60
                    //////////////////////通用判定//////////////////////
                    if (it.type != "霰彈槍" && it.type != "配備" && it.type != "近戰武器" && it.type != "手榴彈" && it.type != "戰場裝備") {
                        if (wkpm > info.kpm * 3.5 && it.name?.indexOf("1895",0,true) == -1){//与生涯KPM差异过大
                            loger.warn("[{}]{}低等级玩家数据异常,疑似改伤 {} 武器KPM大于生涯KPM3.5倍", rank, plname, it.name)
                            player.kick("Low Rank Weapon Anomaly")
                        }
                        if (acc > 70 && wkpm > 1) {//准确率大于50%且kpm>1
                            loger.warn("[{}]{}低等级玩家数据异常,疑似锁腰子 {} 准确率>70%", rank, plname, it.name)
                            player.kick("Low Rank Weapon Anomaly")
                        }
                        if (hs > 0.5 && wkpm > 1) {//爆头率>50%且kpm>1
                            loger.warn("[{}]{}低等级玩家数据异常,爆头异常 {} 爆头率>50%且kpm>1", rank, plname, it.name)
                            player.kick("Low Rank Weapon Anomaly")
                        }
                        if (hs < 0.01) {
                            loger.warn("[{}]{}低等级玩家数据异常,疑似锁腰子 {} 爆头率<1%", rank, plname, it.name)
                            player.kick("Low Rank Weapon Anomaly")
                        }
                    }
                    //////////////////////霰弹枪//////////////////////
                    if (it.type == "霰彈槍") {
                        if (acc > 100) {//霰弹枪命中率>100%
                            loger.warn("[{}]{}低等级玩家数据异常,喷子命中异常 {} 命中率>100%", rank, plname, it.name)
                            player.kick("Low Rank Weapon Anomaly")
                        }
                    }
                    //////////////////////冲锋枪//////////////////////
                    if (it.type == "衝鋒槍"){
                        if (hs > 0.3){
                            loger.warn("[{}]{}低等级玩家数据异常,冲锋枪爆头异常 {} 爆头率>30%", rank, plname, it.name)
                            player.kick("Low Rank Weapon Anomaly")
                        }
                    }
                    //////////////////////轻机枪//////////////////////
                    if (it.type == "輕機槍") {
                        if (
                            it.name?.indexOf("08/18",0,true) != -1 &&
                            it.name?.indexOf("14/17",0,true) != -1 &&
                            it.name?.indexOf("MG15",0,true) != -1 &&
                            it.name?.indexOf("麥德森",0,true) != -1
                            ){
                            if (vp < 3.4) {//小于3.4发子弹打死人
                                loger.warn("[{}]{}低等级玩家数据异常,机枪效率异常 {} 小于3.4发子弹打死人", rank, plname, it.name)
                                player.kick("Low Rank Weapon Anomaly")
                            }
                        }else if (it.name.indexOf("紹沙",0,true) != -1 ){
                            if (vp < 2.5) {//小于2.5发子弹打死人
                                loger.warn("[{}]{}低等级玩家数据异常,机枪效率异常 {} 小于2.5发子弹打死人", rank, plname, it.name)
                                player.kick("Low Rank Weapon Anomaly")
                            }
                        } else{
                            if (vp < 3.7) {//小于4发子弹打死人
                                loger.warn("[{}]{}低等级玩家数据异常,机枪效率异常 {} 小于3.7发子弹打死人", rank, plname, it.name)
                                player.kick("Low Rank Weapon Anomaly")
                            }
                        }
                    }
                }
            }
        }
    }

}