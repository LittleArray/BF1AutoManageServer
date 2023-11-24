package api

import api.GatewayApi.okHttpClient
import config.Config
import data.PostResponse
import okhttp3.Request
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.Proxy

/**
 * @Description
 * @Author littleArray
 * @Date 2023/10/24
 */
object GHSBotsApi {

    data class BotsJson(
        val code: Int,
        val data: Data,
        val message: String
    ){

        data class Data(
            val abnormalCount: Int,
            val bots: List<Bot>,
            val notStartedCount: Int,
            val offlineCount: Int,
            val serverWithBots: List<ServerWithBot>,
            val startingCount: Int,
            val totalCount: Int,
            val usableCount: Int,
            val usedCount: Int
        )
        data class Bot(
            val botNo: String,
            val clientNo: String,
            val gameId: String,
            val onlineState: Int,
            val path: String,
            val state: String,
            val time: Long,
            val user: String?=null
        )
        data class ServerWithBot(
            val botList: List<Bot>,
            val platoonInfo: Any,
            val rspInfo: Any,
            val serverInfo: ServerInfo
        )
        data class ServerInfo(
            val country: String,
            val description: String,
            val gameId: String,
            val guid: String,
            val mapImageUrl: String,
            val mapMode: String,
            val mapModePretty: String,
            val mapName: String,
            val mapNamePretty: String,
            val name: String,
            val region: String,
            val rotation: Any,
            val serverBookmarkCount: String,
            val serverType: String,
            val slots: Slots
        )
        data class Slots(
            val queue: Queue,
            val soldier: Soldier,
            val spectator: Spectator
        )
        data class Queue(
            val current: Int,
            val max: Int
        )
        data class Soldier(
            val current: Int,
            val max: Int
        )
        data class Spectator(
            val current: Int,
            val max: Int
        )
    }

    //BotApi接口
    private val loger: Logger = LoggerFactory.getLogger(this.javaClass)
    fun getBots(url: String): PostResponse {
        return try {
            val request = Request.Builder()
                .url(url)
                .build()
            val response = okHttpClient
                .newBuilder()
                .apply {
                    if (Config.sa != null)
                        proxy(Proxy(Proxy.Type.HTTP, Config.sa))
                }
                .build()
                .newCall(request)
                .execute()
            return if (response.isSuccessful) {
                val res = response.body.string()
                PostResponse(isSuccessful = true, reqBody = res)
            } else {
                val res = response.body.string()
                loger.error("GHSBots列表请求出错,{}",res.replace("\n","").substring(0,20))
                PostResponse(isSuccessful = false, reqBody = res)
            }
        } catch (ex: Exception) {
            loger.error("GHSBots列表请求出错,{}",ex.stackTraceToString().replace("\n","").substring(0,20))
            PostResponse(isSuccessful = false, error = ex.stackTraceToString())
        }
    }

}