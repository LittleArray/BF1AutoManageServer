package api

import com.google.gson.Gson
import data.JsonRpcObj
import data.PLBy22
import data.PostResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

/**
 * @Description
 * @Author littleArray
 * @Date 2023/10/7
 */


object ApiBuilder {
    val okHttpClient = OkHttpClient()
    private val loger: Logger = LoggerFactory.getLogger(this.javaClass)
    fun jsonRpc(body: String, sessionId: String = ""): PostResponse {
        return try {
            val request = Request.Builder()
                .url("https://sparta-gw.battlelog.com/jsonrpc/pc/api")
                .post(body.toRequestBody("application/json".toMediaType()))
                .apply {
                    if (sessionId.isNotBlank()) {
                        addHeader("X-GatewaySession", sessionId)
                    }
                }
                .build()
            val response = okHttpClient.newCall(request).execute()
            return if (response.isSuccessful) {
                val res = response.body.string()
                PostResponse(isSuccessful = true, reqBody = res)
            } else {
                val res = response.body.string()
                loger.error("jsonRpc请求不成功,{}",res)
                PostResponse(isSuccessful = false, reqBody = res)
            }
        } catch (ex: Exception) {
            loger.error("jsonRpc请求出错,{}",ex.stackTraceToString())
            PostResponse(isSuccessful = false, error = ex.stackTraceToString())
        }
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
                    val reason = reason
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

    fun searchServer(name:String,sessionId: String): PostResponse {
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
        return ApiBuilder.jsonRpc(body, sessionId)
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
    fun chooseServerVIP(sessionId: String, persistedGameId: String, levelIndex: String): PostResponse {
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
    //22玩家列表数据接口
    fun getPlayerListBy22(gameId: Long): PLBy22 {
        try {
            val url = "https://blaze.2788.pro/GameManager.getGameDataFromId"
            val json = "{\"DNAM String\": \"csFullGameList\", \"GLST List<Integer>\": [$gameId]}"
            val request = Request.Builder()
                .url(url)
                .post(json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()))
                .addHeader("Accept", "application/json")
                .build()
            val response = okHttpClient
                .newBuilder()
                .connectTimeout(15, TimeUnit.SECONDS)//设置连接超时时间
                .readTimeout(15, TimeUnit.SECONDS)//设置读取超时时间
                .build()
                .newCall(request).execute()
            return if (response.isSuccessful) {
                val res = response.body.string()
                val plBy22 = Gson().fromJson(res, PLBy22::class.java)
                plBy22.copy(isSuccessful = true)
            } else {
                val res = response.body.string()
                loger.error("玩家列表请求不成功,{}",res)
                PLBy22(isSuccessful = false)
            }
        } catch (ex: Exception) {
            loger.error("玩家列表请求出错,{}",ex.stackTraceToString())
            return PLBy22(isSuccessful = false)
        }
    }

}


