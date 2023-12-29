package instance

import api.ApiCore
import api.BFEACApi
import api.BtrApi
import com.google.gson.Gson
import data.BtrMatches
import data.PlayerWeapons
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LowRankChecker(private val player: Player) {

    private val loger: Logger = LoggerFactory.getLogger(this.javaClass)
    val ss = player.serverSetting

    //数据源
    private val info = player.baseInfo!!
    private val id = player._p.NAME
    private val time = info.timePlayed.toDouble() / 60.0 / 60.0
    private val rank = info.rank

    data class WpModel(
        val name: String,
        val kills: Int,
        val time: Double,
        val kpm: Double,
        val acc: Double,
        val hs: Double,
        val efficiency: Double,
        val type: String,
    )

    //异常数据
    private var diff = ""
        set(value) {
            //loger.info("{} => {}",field,value)
            val old = field
            field = value
            if (old == "") {
                eacStateChecker()
            }
            shuaQiang()
        }

    //当前武器
    private var nowWeapon: PlayerWeapons.Weapon? = null
    private var oldWeapon: PlayerWeapons.Weapon? = null

    //双伤服数据
    private var doubleDamage = ""

    //刷枪数据
    private var shuaQiang = ""

    //eacState
    private var eacState = -1

    //权重 满分100 超过100踢出
    private var weight = 0


    fun check() {
        if (time < 65 && ss.get().whitelist.none { it == id }) {
            if(info.kpm < 0.1) return
            lifeDataChecker()
            val weapons = ApiCore.getWeapons(player.pid.toString())
            weapons?.data?.let { data ->
                var index = 0
                for (it in data) {
                    if (index > 20) break
                    nowWeapon = it
                    val kills = (it.kills ?: 0)//击杀数
                    val acc = it.accuracy ?: 0.0//命中率
                    var vp = (it.hits?.toDouble()?.div(kills.toDouble()) ?: 999.0)//效率
                    vp = if (vp == 0.0) 999.0 else vp
                    val hs = try {
                        ((it.headshots?.toDouble() ?: 0.0) / (it.kills?.toDouble() ?: 0.0)).times(100)
                    } catch (e: Exception) {
                        0.0
                    }//武器爆头率
                    val wkpm = (it.kills?.toDouble() ?: 0.0) / (it.seconds?.div(60.0) ?: 0.0)//武器KPM
                    val wtime = (it.seconds ?: 1.0) / 60 //武器时长单位分钟
                    if (kills < 60 || wtime < 10) continue//时长和击杀限制
                    if (it.name?.contains("1895（步兵）") == true) continue//去除1985的检测
                    val model = WpModel(it.name ?: "", kills, wtime, wkpm, acc, hs, vp, it.type ?: "")
                    index++
                    when (it.type) {
                        "霰彈槍" -> shotGun(model)
                        "衝鋒槍" -> subMachineGun(model)
                        "步槍" -> rifle(model)
                        "半自動步槍" -> semiAutomaticRifles(model)
                    }
                    weaponGeneralDataChecker(model)
                    efficiencyChecker(model)
                }
            }
        }
        ////////////////////总结/////////////////////
        if (diff.isEmpty() || eacState == 1) return
        val diffs = diff.split("&&").filter { it.isNotEmpty() }
        val shuaQiangs = shuaQiang.split("&&").filter { it.isNotEmpty() }
        val doubleDamages = doubleDamage.split("&&").filter { it.isNotEmpty() }
        diffs.forEach {
            loger.warn("[{}h] {} PID: {} {}", time.toInt(), id, player._p.PID, it)
        }
        shuaQiangs.forEach {
            loger.warn("[{}h] {} PID: {} {}", time.toInt(), id, player._p.PID, it)
        }
        doubleDamages.forEach {
            loger.warn("[{}h] {} PID: {} {}", time.toInt(), id, player._p.PID, it)
        }
        val kick = if (ss.get().lrcKick) weight > 100 else false
        val debug = if (ss.get().lrcKick) weight > 100 else false
        if (kick) {
            loger.info("经过判定踢出玩家 {}", id)
            player.kick("Low Rank Weapon Anomaly")
        }
        if (diff.isNotEmpty()) {
            ServerInstance.INSTANCE.forEach {
                if (it.serverSetting.gameId == player.serverSetting.get().gameId) {
                    it.lrcLog =
                        Server.LRCLog(
                            id, System.currentTimeMillis(), time.toLong(), kick,
                            " 异常数据: \n $diff 刷枪数据: \n $shuaQiang 双伤服数据: \n $doubleDamage 权重:$weight"
                        )
                }
            }
        }
    }

    /**
     * 生涯数据检查
     */
    private fun lifeDataChecker() {
        if (info.kills > 100) {
            if (info.accuracy > 0.5) {
                diff += "异常生涯准确率>50%&&"
                weight += 25
            }
            if ((info.headShots.toDouble() / info.kills.toDouble()) > 0.5) {
                diff += "异常生涯爆头率>50%&&"
                weight += 25
            }
        }
    }


    /**
     * eac状态检查
     */
    private fun eacStateChecker() {
        val eac = BFEACApi.getEacState(id)
        if (eac?.error_code == 0) {
            eacState = eac.data?.firstOrNull()?.current_status ?: -1
            if (eacState == 1) weight += 101
            if (eacState == 2 || eacState == 0) {
                diff += "请注意BFEAC存在此玩家的案件 https://www.bfeac.com/#/case/${eac.data?.firstOrNull()?.case_id} 未处理或证据不足 &&"
                weight += 100
            }

        }
    }

    /**
     * 刷枪判定
     */
    private fun shuaQiang() {
        if (nowWeapon != oldWeapon)
            WeaposTask.hm.forEach { (wpName, k) ->
                if (nowWeapon?.name?.contains(wpName) == true) {
                    shuaQiang += "${nowWeapon!!.name} 可能刷枪用于解锁 ${k.replace(" ", " 条件 ")} &&"
                    oldWeapon = nowWeapon
                }
            }
    }


    /**
     * 效率检测
     */
    private fun efficiencyChecker(it: WpModel) {
        //冲锋枪 机枪 半自动 别的不判
        var isDoubleDamage: Boolean? = null
        EfficiencyMap.hm.forEach { (wpName, k) ->
            if (it.name.contains(wpName)) {
                val kV = 100.0 / k
                if (it.efficiency < kV - 0.3 && it.kpm > 1.8 && it.kills > 99 || it.efficiency < 1.0) {
                    if (isDoubleDamage == null) isDoubleDamage = btrMatchChecker() ?: false
                    if (isDoubleDamage == false) {
                        diff += "${it.name} 效率异常 &&"
                        weight += 20
                    }
                }
            }
        }
    }

    /**
     * Btr最近对局检查
     */
    private fun btrMatchChecker(): Boolean? {
        BtrApi.getRec(id).takeIf { it.isSuccessful }?.reqBody?.let {
            Gson().fromJson(it, BtrMatches::class.java)?.let {
                it.data.matches.filter {
                    it.metadata.serverName.contains("200%", true) || it.metadata.serverName.contains("shua", true)
                }.takeIf { it.isNotEmpty() }?.let {
                    var old = ""
                    it.forEach {
                        if (it.metadata.serverName != old)
                            doubleDamage += "${it.metadata.serverName} &&"
                        old = it.metadata.serverName
                    }
                    return it.isNotEmpty()
                }
                return false
            }
        }
        return null
    }

    /**
     * 霰弹枪判定
     */
    private fun shotGun(it: WpModel) {
        if (it.acc > 150) {//霰弹枪命中率>100%
            diff += "${it.name} 喷子命中异常 &&"
            weight += 35
        }
        if (!it.name.contains("霰彈塊") && it.hs > 40) {
            diff += "${it.name} 喷子爆頭异常 &&"
            weight += 55
        }
    }

    /**
     * 衝鋒槍判定
     */
    private fun subMachineGun(it: WpModel) {
        if (it.hs > 20 && it.kpm > 1.8) {
            diff += "${it.name} 冲锋枪爆头异常 &&"
            weight += 25
            if (it.hs > 25) {
                weight += 40
            }
        }
    }

    /**
     * 步槍判定
     */
    private fun rifle(it: WpModel) {
        /*if (it.efficiency < 1.1 && it.kpm > 1.8) {
            diff += "${it.name} 效率异常 &&"
            weight += 25
        }*/
    }
    /**
     * 半自動步槍判定
     */
    private fun semiAutomaticRifles(it: WpModel) {
        var nDiff = ""
        if (it.kpm < 1.81) return
        if (it.name.contains("RSC")) {
            if (it.hs > 20 ){
                nDiff += "RSC 爆头异常"
                weight += 25
            }
        }else{
            if (it.hs > 25){
                nDiff += "${it.name} 爆头异常"
                if (it.hs > 30){
                    weight += 25
                }
                weight += 25
            }
        }
    }

    /**
     * 武器通用数据检查
     */
    private fun weaponGeneralDataChecker(it: WpModel) {

        if (it.type != "霰彈槍" && it.type != "配備" && it.type != "近戰武器" && it.type != "手榴彈" && it.type != "戰場裝備" && it.type != "佩槍") {
            var nDiff = ""
            if (it.kpm > info.kpm * 4 && !it.name.contains("1895")) {//与生涯KPM差异过大
                nDiff += "与生涯KPM差异过大 "
                weight += 10
            }
            if (it.acc > 70 && it.kpm > 1.8) {//准确率大于70%且kpm>1.8
                nDiff += "准确率过高 "
                weight += 25
            }
            if (it.hs > 60 && it.kpm > 1.8 || it.hs > 85) {//爆头率>60%且kpm>1.8或爆头率>85%
                nDiff += "爆头异常 "
                weight += 45
            }
            if (it.hs < 1 && it.kpm > 1.8) {
                nDiff += "疑似锁腰子 "
                weight += 20
            }
            if (nDiff.isNotEmpty())
                diff += "${it.name} $nDiff &&"
        }
    }

}