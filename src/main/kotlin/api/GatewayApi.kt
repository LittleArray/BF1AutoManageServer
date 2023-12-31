package api

import com.google.gson.Gson
import config.GConfig.sa
import data.FullServerInfoJson
import data.JsonRpcObj
import data.PostResponse
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utils.ChineseTR.toTradition
import java.net.Proxy
import java.util.concurrent.TimeUnit


/**
 * @Description
 * @Author littleArray
 * @Date 2023/10/7
 */


object GatewayApi {
    val okHttpClient = OkHttpClient()

    val taskQueue = Channel<suspend () -> PostResponse>(2)
    private val loger: Logger = LoggerFactory.getLogger(this.javaClass)
    fun jsonRpc(body: String, sessionId: String = ""): PostResponse = runBlocking {
        launch {
            taskQueue.send {
                runBlocking {
                    try {
                        val request = Request.Builder()
                            .url("https://sparta-gw.battlelog.com/jsonrpc/pc/api")
                            .post(body.toRequestBody("application/json".toMediaType()))
                            .apply {
                                if (sessionId.isNotBlank()) {
                                    addHeader("X-GatewaySession", sessionId)
                                }
                            }
                            .build()
                        val response = okHttpClient
                            .newBuilder()
                            .apply {
                                if (sa != null)
                                    proxy(Proxy(Proxy.Type.HTTP, sa))
                            }
                            .connectTimeout(30, TimeUnit.SECONDS)//设置连接超时时间
                            .readTimeout(30, TimeUnit.SECONDS)//设置读取超时时间
                            .build().newCall(request).execute()
                        if (response.isSuccessful) {
                            val res = response.body.string()
                            PostResponse(isSuccessful = true, reqBody = res)
                        } else {
                            val res = response.body.string()
                            loger.error("jsonRpc请求不成功,{}", res.replace("\n", ""))
                            PostResponse(isSuccessful = false, reqBody = res)
                        }
                    } catch (ex: Exception) {
                        loger.error("jsonRpc请求出错,{}", ex.stackTraceToString().replace("\n", "").substring(0, 20))
                        PostResponse(isSuccessful = false, error = ex.stackTraceToString())
                    }
                }
            }
        }
        taskQueue.receive().invoke()
    }

    //踢人
    fun kickPlayer(sessionId: String, gameId: String, personaId: String, reason: String): PostResponse {
        val method = "RSP.kickPlayer"
        val body = Gson().toJson(
            JsonRpcObj(
                method = method,
                params = object {
                    val game = "tunguska"
                    val gameId = gameId
                    val personaId = personaId
                    val reason = reason.toTradition()
                }
            )
        )
        return jsonRpc(body, sessionId)
    }

    //addBan
    fun addServerBan(sessionId: String, RSPserverId: Int, personaName: String): PostResponse {
        val method = "RSP.addServerBan"
        val body = Gson().toJson(
            JsonRpcObj(
                method = method,
                params = object {
                    val game = "tunguska"
                    val serverId = RSPserverId
                    val personaName = personaName
                }
            )
        )
        return jsonRpc(body, sessionId)
    }

    fun searchServer(name: String, sessionId: String): PostResponse {
        val method = "GameServer.searchServers"
        val body = Gson().toJson(
            JsonRpcObj(
                method = method,
                params = object {
                    val game = "tunguska"
                    val filterJson = "{\"name\":\"$name\"}"
                    val limit = 200
                }
            )
        )
        return GatewayApi.jsonRpc(body, sessionId)
    }

    //removeBan
    fun removeServerBan(sessionId: String, RSPserverId: Int, personaId: String): PostResponse {
        val method = "RSP.removeServerBan"
        val body = Gson().toJson(
            JsonRpcObj(
                method = method,
                params = object {
                    val game = "tunguska"
                    val serverId = RSPserverId
                    val personaId = personaId
                }
            )
        )
        return jsonRpc(body, sessionId)
    }

    //addVIP
    fun addServerVIP(sessionId: String, RSPserverId: Int, personaName: String): PostResponse {
        val method = "RSP.addServerVip"
        val body = Gson().toJson(
            JsonRpcObj(
                method = method,
                params = object {
                    val game = "tunguska"
                    val serverId = RSPserverId
                    val personaName = personaName
                }
            )
        )
        return jsonRpc(body, sessionId)
    }

    //removeVIP
    fun removeServerVIP(sessionId: String, RSPserverId: Int, personaId: String): PostResponse {
        val method = "RSP.removeServerVip"
        val body = Gson().toJson(
            JsonRpcObj(
                method = method,
                params = object {
                    val game = "tunguska"
                    val serverId = RSPserverId
                    val personaId = personaId
                }
            )
        )
        return jsonRpc(body, sessionId)
    }

    //切图
    fun chooseServerMap(sessionId: String, persistedGameId: String, levelIndex: String): PostResponse {
        val method = "RSP.chooseLevel"
        val body = Gson().toJson(
            JsonRpcObj(
                method = method,
                params = object {
                    val game = "tunguska"
                    val persistedGameId = persistedGameId
                    val levelIndex = levelIndex
                }
            )
        )
        return jsonRpc(body, sessionId)
    }

    //换边
    fun movePlayer(sessionId: String, gameId: String, personaId: Long, teamId: Int): PostResponse {
        val method = "RSP.movePlayer"
        val body = Gson().toJson(
            JsonRpcObj(
                method = method,
                params = object {
                    val game = "tunguska"
                    val gameId = gameId
                    val personaId = personaId
                    val teamId = teamId
                }
            )
        )
        return jsonRpc(body, sessionId)
    }

    //获取服务器完整信息
    fun getFullServerDetails(sessionId: String, gameId: String): FullServerInfoJson {
        val method = "GameServer.getFullServerDetails"
        val body = Gson().toJson(
            JsonRpcObj(
                method = method,
                params = object {
                    val game = "tunguska"
                    val gameId = gameId
                }
            )
        )
        val postResponse = jsonRpc(body, sessionId)
        return if (postResponse.isSuccessful) {
            Gson().fromJson(postResponse.reqBody, FullServerInfoJson::class.java).copy(isSuccessful = true)
        } else {
            FullServerInfoJson(isSuccessful = false)
        }
    }
}


