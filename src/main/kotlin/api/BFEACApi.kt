package api

import api.GatewayApi.okHttpClient
import com.google.gson.Gson
import config.GConfig
import data.EacInfoJson
import data.PostResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.Proxy

/**
 * @Description
 * @Author littleArray
 * @Date 2023/10/24
 */
object BFEACApi {
    private val loger: Logger = LoggerFactory.getLogger(this.javaClass)

    data class MultiCheckPostJson(
        val pids: MutableList<Long> = mutableListOf()
    )

    data class MultiCheckResponse(
        val data: List<Long>,
        val error_code: Int,
        val error_msg: Any
    )

    //EacApi接口
    private fun postEacApi(url: String, body: String, apikey: String = ""): PostResponse {
        return try {
            val request = Request.Builder()
                .url(url)
                .post(body.toRequestBody("application/json".toMediaType()))
                .apply {
                    if (apikey.isNotBlank()) {
                        addHeader("apikey", apikey)
                    }
                }
                .build()
            val response = okHttpClient
                .newBuilder()
                .apply {
                    if (GConfig.sa != null)
                        proxy(Proxy(Proxy.Type.HTTP, GConfig.sa))
                }
                .build()
                .newCall(request)
                .execute()
            return if (response.isSuccessful) {
                val res = response.body.string()
                PostResponse(isSuccessful = true, reqBody = res)
            } else {
                val res = response.body.string()
                loger.error("BFEAC请求不成功")
                PostResponse(isSuccessful = false, reqBody = res)
            }
        } catch (ex: Exception) {
            loger.error("BFEAC请求不成功 {}",ex.stackTraceToString())
            PostResponse(isSuccessful = false, error = ex.stackTraceToString())
        }
    }
    private fun getEacApi(url: String): PostResponse {
        return try {
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            val response = okHttpClient
                .newBuilder()
                .apply {
                    if (GConfig.sa != null)
                        proxy(Proxy(Proxy.Type.HTTP, GConfig.sa))
                }
                .build()
                .newCall(request)
                .execute()
            return if (response.isSuccessful) {
                val res = response.body.string()
                PostResponse(isSuccessful = true, reqBody = res)
            } else {
                val res = response.body.string()
                loger.error("BFEAC请求不成功")
                PostResponse(isSuccessful = false, reqBody = res)
            }
        } catch (ex: Exception) {
            loger.error("BFEAC请求不成功 {}",ex.stackTraceToString())
            PostResponse(isSuccessful = false, error = ex.stackTraceToString())
        }
    }

    //eac批量查询
    fun multiCheck(pids: MultiCheckPostJson): MultiCheckResponse {
        val body = Gson().toJson(pids, MultiCheckPostJson::class.java)
        val postResponse = postEacApi("https://api.bfeac.com/global_banlist/check/multi/pid", body, "")
        return if (postResponse.isSuccessful) {
            Gson().fromJson(postResponse.reqBody, MultiCheckResponse::class.java)
        } else {
            MultiCheckResponse(error_code = 404, data = listOf(), error_msg = "")
        }
    }
    fun getEacState(id:String): EacInfoJson?{
        val response = getEacApi("https://api.bfeac.com/case/EAID/$id")
        if (response.isSuccessful){
            return try {
                Gson().fromJson(response.reqBody, EacInfoJson::class.java)
            }catch (e:Exception){
                null
            }
        }
        return null
    }
}