package api

import com.google.gson.Gson
import data.GatewaySessionId
import data.JsonRpcObj
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @Description
 * @Author littleArray
 * @Date 2023/10/16
 */
object GatewayUtils {
    private val loger: Logger = LoggerFactory.getLogger(this.javaClass)
    fun update(){
        /*if (config.gatewaySid.isNotBlank() && config.gatewayRemid.isNotBlank()){
            loger.info("update gateway authToken...")
            val authToken = getAuthToken(config.gatewaySid, config.gatewayRemid)
            config.gatewayAuthToken = authToken?.access_token?:""
            config.gatewaySid = authToken?.sid?:""
            config.gatewaySessionId = getSessionId()?:""
            FileUtil.saveConfig(config)
        }*/
    }

    /**
     * 获取登录需要的AuthToken
     * @param sid String
     * @param remid String
     * @return String
     */
    private fun getLoginAuthToken(sid: String, remid: String): String {
        loger.info("更新AuthToken")
        val request = Request.Builder()
            .url("https://accounts.ea.com/connect/auth;?client_id=sparta-backend-as-user-pc&response_type=code&release_type=none")
            .addHeader("Cookie","sid=$sid; remid=$remid")
            .build()
        val response = OkHttpClient().newBuilder().build().newCall(request).execute()
        val authToken = response.request.url.toString().replace("http://127.0.0.1/success?code=", "")
        loger.info("AuthToken:{}",authToken)
        return authToken
    }

    /**
     * 获取Gateway的SessionId
     * @param sid String
     * @param remid String
     * @return String?
     */
    fun getSessionId(sid: String, remid: String):String?{
        val method = "Authentication.getEnvIdViaAuthCode"
        val body = Gson().toJson(
            JsonRpcObj(
                method = method,
                params = object {
                    val authCode = getLoginAuthToken(sid, remid)
                    val locale = "zh-tw"
                    val product = null
                    val dtab = null
                    val branchName = "Tunguska"
                    val changeListNumber = "3779779"
                    val minutesToUTC = -480
                }
            )
        )
        val request = Request.Builder()
            .url("https://sparta-gw.battlelog.com/jsonrpc/pc/api")
            .post(body.toRequestBody("application/json".toMediaType()))
            .apply {
                addHeader("X-Guest", "no-session-id")
                addHeader("X-HostingGameId", "tunguska")
            }
            .build()
        val response = GatewayApi.okHttpClient.newCall(request).execute()
        val res = if (response.isSuccessful) {
            loger.info("登入成功")
            response.body.string()
        } else {
            val err = response.body.string()
            loger.error("登入请求失败:{}",err.replace("\n","").substring(0,36))
            return null
        }
        return try {
            Gson().fromJson(res, GatewaySessionId::class.java).result.sessionId
        }catch (e:Exception){
            loger.error("gson err,{}",e.stackTraceToString())
            null
        }
    }
}