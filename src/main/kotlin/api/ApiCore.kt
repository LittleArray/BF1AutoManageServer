package api

import com.google.gson.Gson
import config.GConfig.Config
import data.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit


/**
 * @Description
 * @Author littleArray
 * @Date 2023/10/18
 */
object ApiCore {
    data class PostResponse(
        var isSuccessful: Boolean = false,
        var error: String = "",
        var reqBody: String = "",
    )
    val okHttpClient = OkHttpClient()
    //数据Api接口
    fun build(url: String): PostResponse {
        return try {
            val request = Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .build()
            val response = okHttpClient
                .newBuilder()
                .connectTimeout(15, TimeUnit.SECONDS)//设置连接超时时间
                .readTimeout(15, TimeUnit.SECONDS)//设置读取超时时间
                .build()
                .newCall(request).execute()
            if (response.isSuccessful) {
                PostResponse(isSuccessful = true, reqBody = response.body.string())
            } else {
                PostResponse(isSuccessful = false, reqBody = response.body.string())
            }
        } catch (ex: Exception) {
            PostResponse(isSuccessful = false, error = ex.stackTraceToString())
        }
    }


    fun getBaseInfo(pid:String,iswpvp:String = "true"):PlayerBaseInfo?{
        val response = build("${Config.serverUrl}/gateway/getBaseInfo/$pid/${iswpvp}")
        if (response.isSuccessful){
            return try {
                Gson().fromJson(response.reqBody,PlayerBaseInfo::class.java)
            }catch (e:Exception){
                null
            }
        }
        return null
    }
    class RspData : ArrayList<String>()
    fun getAdmin(pid:String):MutableSet<String>?{
        val response = build("${Config.serverUrl}/gateway/getAdmin/$pid/")
        if (response.isSuccessful){
            return try {
                Gson().fromJson(response.reqBody,RspData::class.java).toMutableSet()
            }catch (e:Exception){
                null
            }
        }
        return null
    }

    fun getVip(pid:String):MutableSet<String>?{
        val response = build("${Config.serverUrl}/gateway/getVip/$pid/")
        if (response.isSuccessful){
            return try {
                Gson().fromJson(response.reqBody,RspData::class.java).toMutableSet()
            }catch (e:Exception){
                null
            }
        }
        return null
    }

    fun getBan(pid:String):MutableSet<String>?{
        val response = build("${Config.serverUrl}/gateway/getBan/$pid/")
        if (response.isSuccessful){
            return try {
                Gson().fromJson(response.reqBody,RspData::class.java).toMutableSet()
            }catch (e:Exception){
                null
            }
        }
        return null
    }

    fun getVehicles(pid:String):PlayerVehicles?{
        val response = build("${Config.serverUrl}/gateway/getVehicles/$pid")
        if (response.isSuccessful){
            return try {
                Gson().fromJson(response.reqBody,PlayerVehicles::class.java)
            }catch (e:Exception){
                null
            }
        }
        return null
    }

    fun getWeapons(pid:String):PlayerWeapons?{
        val response = build("${Config.serverUrl}/gateway/getWeapons/$pid")
        if (response.isSuccessful){
            return try {
                Gson().fromJson(response.reqBody,PlayerWeapons::class.java)
            }catch (e:Exception){
                null
            }
        }
        return null
    }

}