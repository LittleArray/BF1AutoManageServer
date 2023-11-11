package command

import api.BtrApi
import api.GHSBotsApi
import instance.ServerInstance
import api.GatewayApi
import instance.Server
import com.google.gson.Gson
import data.GatewayServerSearch
import instance.Player
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

/**
 * @Description
 * @Author littleArray
 * @Date 2023/10/21
 */
object Command {
    private val loger: Logger = LoggerFactory.getLogger(this.javaClass)
    var cmd: String = ""
        set(value) {
            field = value
            cmd(value)
        }

    fun cmd(cmd: String) {
        val split = cmd.split(" ")
        when (split[0]) {
            "add" -> {
                val gameID = try {
                    split.getOrNull(1)?.toLong()
                } catch (e: Exception) {
                    loger.error("非法参数")
                    return
                }
                val sessionID = split.getOrNull(2)
                if (gameID == null || sessionID == null) {
                    loger.error("缺少参数")
                    return
                }
                if (ServerInstance.addServer(Server.ServerSetting(sessionID = sessionID, gameId = gameID))) {
                    loger.info("添加成功")
                } else {
                    loger.info("添加失败")
                }
            }

            "remove" -> {
                val gameID = try {
                    split.getOrNull(1)?.toLong()
                } catch (e: Exception) {
                    loger.error("非法参数")
                    return
                }
                if (gameID == null) {
                    loger.error("缺少参数")
                    return
                }
                if (ServerInstance.removeServer(gameID)) {
                    loger.info("移除成功")
                } else {
                    loger.info("移除失败")
                }
            }

            "list" -> {
                ServerInstance.INSTANCE.forEach {
                    loger.info("服务器{}", it.serverSetting.gameId)
                }
            }

            "ss" -> {
                val reqBody = GatewayApi.searchServer(
                    split.getOrNull(1) ?: "",
                    split.getOrNull(2) ?: ServerInstance.INSTANCE.firstOrNull()?.serverSetting?.sessionID ?: ""
                ).reqBody
                Gson().fromJson(reqBody, GatewayServerSearch::class.java).result.gameservers.forEach {
                    loger.info("服名:${it.name} GameID:${it.gameId} ${it.mapNamePretty} - ${it.mapModePretty}")
                }
            }

            "update" -> {
                ServerInstance.INSTANCE.forEach {
                    it.updateSessionID()
                }
            }

            "boom" -> {
                val gameID = try {
                    split.getOrNull(1)?.toLong()
                } catch (e: Exception) {
                    loger.error("非法参数")
                    return
                }
                if (gameID == null) {
                    loger.error("缺少参数")
                    return
                }
                ServerInstance.INSTANCE.forEach {
                    if (it.serverSetting.gameId == gameID) {
                        it.playerList.forEach {
                            it.kick(split.getOrNull(2) ?: "server shutdown 关服了")
                        }
                    }
                }
            }

            "aclassl" -> {
                val gameID = try {
                    split.getOrNull(1)?.toLong()
                } catch (e: Exception) {
                    loger.error("非法参数")
                    return
                }
                val className = split.getOrNull(2)
                val rank = try {
                    split.getOrNull(3)?.toInt()
                } catch (e: Exception) {
                    loger.error("非法参数")
                    return
                }
                if (gameID == null || className == null || rank == null) {
                    loger.error("缺少参数")
                    return
                }
                ServerInstance.INSTANCE.forEach {
                    if (it.serverSetting.gameId == gameID) {
                        it.serverSetting.classRankLimited.put(className, rank)
                    }
                }
            }

            "awl" -> {
                val gameID = try {
                    split.getOrNull(1)?.toLong()
                } catch (e: Exception) {
                    loger.error("非法参数")
                    return
                }
                val name = split.getOrNull(2)
                if (gameID == null || name == null) {
                    loger.error("缺少参数")
                    return
                }
                ServerInstance.INSTANCE.forEach {
                    if (it.serverSetting.gameId == gameID) {
                        it.serverSetting.whitelist.add(name)
                    }
                }
            }

            "aghsbots" -> {
                val gameID = try {
                    split.getOrNull(1)?.toLong()
                } catch (e: Exception) {
                    loger.error("非法参数")
                    return
                }
                val url = split.getOrNull(2)
                if (gameID == null || url == null) {
                    loger.error("缺少参数")
                    return
                }
                ServerInstance.INSTANCE.forEach {
                    if (it.serverSetting.gameId == gameID) {
                        val bots = GHSBotsApi.getBots(url)
                        if (bots.isSuccessful) {
                            val botsJson = Gson().fromJson(bots.reqBody, GHSBotsApi.BotsJson::class.java)
                            botsJson.data.bots.forEach { b ->
                                if (b.user != null)
                                    it.serverSetting.botlist.add(b.user)
                            }
                            loger.info("机器人数据导入成功{}", gameID)
                        }
                    }
                }
            }

            "qt" -> {
                val gameID = try {
                    split.getOrNull(1)?.toLong()
                } catch (e: Exception) {
                    loger.error("非法参数")
                    return
                }
                val index = try {
                    split.getOrNull(2)?.toInt()
                } catch (e: Exception) {
                    loger.error("非法参数")
                    return
                }
                if (gameID == null || index == null) {
                    loger.error("缺少参数")
                    return
                }
                ServerInstance.INSTANCE.forEach {
                    if (it.serverSetting.gameId == gameID) {
                        val map = it.chooseMap(index)
                        loger.info("{}切图成功:{}", gameID, map)
                    }
                }

            }

            "grsp" -> {
                val gameID = try {
                    split.getOrNull(1)?.toLong()
                } catch (e: Exception) {
                    loger.error("非法参数")
                    return
                }
                ServerInstance.INSTANCE.forEach {
                    if (it.serverSetting.gameId == gameID) {
                        val result = it.getRSPInfo()
                        loger.info("{}地图池:", gameID)
                        result?.serverInfo?.rotation?.forEachIndexed { index, it ->
                            loger.info("{}-{} {}", it.mapPrettyName, it.modePrettyName, index)
                        }
                    }
                }
            }

            "k" -> {
                val list = mutableListOf<Player>()
                ServerInstance.INSTANCE.forEach {
                    it.playerList.forEach {
                        if (it._p.NAME.indexOf((split.getOrNull(1) ?: ""), 0, true) != -1) {
                            list.add(it)
                        }
                    }
                }
                if (list.size == 1) {
                    list.first().kick(split.getOrNull(2) ?: "Rule Violation")
                } else {
                    loger.info("多个或没有找到对应玩家")
                    list.forEach {
                        loger.info("{}", it._p.NAME)
                    }
                }
            }

            "kid" -> {
                val pid = split.getOrNull(1) ?: ""
                ServerInstance.INSTANCE.forEach {
                    val kick = GatewayApi.kickPlayer(
                        it.serverSetting.sessionID,
                        it.serverSetting.gameId.toString(),
                        pid,
                        split.getOrNull(2) ?: "Rule Violation"
                    )
                    if (kick.isSuccessful) loger.info(
                        "在服务器{}踢出{}成功",
                        it.serverSetting.gameId,
                        pid
                    ) else loger.error("在服务器{}踢出{}失败", it.serverSetting.gameId, pid)
                }
            }

            "b" -> {
                val gameID = try {
                    split.getOrNull(1)?.toLong()
                } catch (e: Exception) {
                    loger.error("非法参数")
                    return
                }
                val name = try {
                    split.getOrNull(2)
                } catch (e: Exception) {
                    loger.error("非法参数")
                    return
                }
                if (gameID == null || name == null) {
                    loger.error("缺少参数")
                    return
                }
                ServerInstance.INSTANCE.forEach {
                    if (it.serverSetting.gameId == gameID) {
                        val rspInfo = it.getRSPInfo()
                        val ban = GatewayApi.addServerBan(
                            it.serverSetting.sessionID,
                            rspInfo?.rspInfo?.server?.serverId?.toInt() ?: 0,
                            name
                        )
                        if (ban.isSuccessful) loger.info("{}封禁成功", name)
                    }
                }
            }

            "stop" -> {
                ServerInstance.save()
                exitProcess(0)
            }

            "ls" -> {
                ServerInstance.INSTANCE.forEach {
                    val rspInfo = it.getRSPInfo()
                    loger.info("服务器{} {}", it.serverSetting.gameId, rspInfo?.serverInfo?.name)
                    loger.info(
                        "地图模式:{}-{} 收藏数:{} {}/{}[{}]({})",
                        rspInfo?.serverInfo?.mapNamePretty,
                        rspInfo?.serverInfo?.mapModePretty,
                        rspInfo?.serverInfo?.serverBookmarkCount,
                        rspInfo?.serverInfo?.slots?.Soldier?.current,
                        rspInfo?.serverInfo?.slots?.Soldier?.max,
                        rspInfo?.serverInfo?.slots?.Queue?.current,
                        rspInfo?.serverInfo?.slots?.Spectator?.current,
                    )
                    it.playerList.forEach {
                        loger.info(
                            "[{}] {} LKD:{} LKP:{} RKD:{} RKP:{} 队伍:{} 语言地区:{} {}ms",
                            it._p.PATT?.rank,
                            it._p.NAME,
                            it.lkd,
                            it.lkp,
                            it.rkd,
                            it.rkp,
                            it._p.TEAMNAME,
                            it._p.LOC.toString(16).chunked(2).map { it.toInt(16).toByte() }.toByteArray()
                                .toString(Charsets.US_ASCII),
                            it._p.PATT?.latency
                        )
                    }
                }
            }
            "btro" ->{
                loger.info("btr队列:{}",BtrApi.taskQueue)
            }
            "jso" ->{
                loger.info("JsonRpc队列:{}",GatewayApi.taskQueue)
            }
            else -> loger.info("无效命令")
        }
    }
}