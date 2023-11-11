package api

import command.Command
import instance.Player
import instance.ServerInstance
import io.javalin.Javalin
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

/**
 * @Description
 * @Author littleArray
 * @Date 2023/11/5
 */
object QQBotApi {
    val app = Javalin.create(/*config*/)
    private val loger: Logger = LoggerFactory.getLogger(this.javaClass)

    data class Res(
        val newToken: String,
        val data: String,
    )

    fun init(port: Int) {
        app.start(port)
        listen()
    }

    fun listen() {
        app.get("/{token}/{gameid}/{method}/{param}") { ctx ->
            run p@{
                val token = ctx.pathParam("token")
                val gameid = try {
                    ctx.pathParam("gameid").toLong()
                } catch (e: Exception) {
                    ctx.status(404)
                    ctx.json(Res("", "非法GameID"))
                    return@p
                }
                val method = ctx.pathParam("method")
                val param = ctx.pathParam("param").split(",")
                loger.info("token:{},gameid:{},method:{},param:{}", token, gameid, method, param)
                ServerInstance.INSTANCE.forEach {
                    if (it.serverSetting.token == token) {
                        if (it.serverSetting.gameId == gameid) {
                            val newToken = UUID.randomUUID().toString()
                            it.serverSetting.token = newToken
                            when (method) {
                                "kick" -> {
                                    val name = param.getOrNull(0)
                                    if (name == null) {
                                        ctx.status(404)
                                        ctx.json(Res(newToken, "无效param"))
                                        return@p
                                    }
                                    val list = mutableListOf<Player>()
                                    it.playerList.forEach {
                                        if (it._p.NAME.indexOf(name, 0, true) != -1) {
                                            list.add(it)
                                        }
                                    }
                                    if (list.size == 1) {
                                        list.first().kick(param.getOrNull(1) ?: "Rule Violation")
                                        ctx.json(Res(newToken, list.first()._p.NAME))
                                    } else {
                                        var names = ""
                                        list.forEach {
                                            names += it._p.NAME + ","
                                        }
                                        ctx.status(500)
                                        ctx.json(Res(newToken, names))
                                    }
                                }

                                "ban" -> {
                                    val name = param.getOrNull(0)
                                    if (name == null) {
                                        ctx.status(404)
                                        ctx.json(Res(newToken, "无效param"))
                                        return@p
                                    }
                                    val rspInfo = it.getRSPInfo()
                                    val ban = GatewayApi.addServerBan(
                                        it.serverSetting.sessionID,
                                        rspInfo?.rspInfo?.server?.serverId?.toInt() ?: 0,
                                        name
                                    )
                                    if (ban.isSuccessful) {
                                        ctx.json(Res(newToken, name))
                                    } else {
                                        ctx.status(500)
                                        ctx.json(Res(newToken, ban.reqBody))
                                    }
                                }

                                "qt" -> {
                                    val index = try {
                                        param.getOrNull(2)?.toInt()
                                    } catch (e: Exception) {
                                        ctx.status(404)
                                        ctx.json(Res(newToken, "无效param"))
                                        return@p
                                    }
                                    if (index == null) {
                                        ctx.json(Res(newToken, "缺少参数"))
                                        return@p
                                    }

                                    val map = it.chooseMap(index)

                                    ctx.json(Res(newToken, map?:""))
                                }

                                else -> {
                                    ctx.status(404)
                                    ctx.json(Res(newToken, "无效method"))
                                }
                            }
                            return@p
                        }
                    }
                }
                ctx.status(500)
                ctx.json(Res("", "无效token或无效GameID"))
            }
        }
    }

}