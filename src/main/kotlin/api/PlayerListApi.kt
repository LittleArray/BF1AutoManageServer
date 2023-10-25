package api

import com.google.gson.Gson
import data.PLBy22
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.Proxy
import java.util.concurrent.TimeUnit

/**
 * @Description
 * @Author littleArray
 * @Date 2023/10/24
 */
object PlayerListApi {
    private val loger: Logger = LoggerFactory.getLogger(this.javaClass)
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
            val response = GatewayApi.okHttpClient
                .newBuilder()
                .proxy(Proxy(Proxy.Type.HTTP, GatewayApi.sa))
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
                loger.error("玩家列表请求不成功,{}",res.replace("\n","").substring(0,36))
                PLBy22(isSuccessful = false)
            }
        } catch (ex: Exception) {
            loger.error("玩家列表请求出错,{}",ex.stackTraceToString().replace("\n","").substring(0,36))
            return PLBy22(isSuccessful = false)
        }
    }
}