package api

import api.GatewayApi.okHttpClient
import com.google.gson.Gson
import data.BtrMatch
import data.BtrMatches
import data.PostResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.Proxy
import java.util.*
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
/**
 * @Description
 * @Author littleArray
 * @Date 2023/10/19
 */
object BtrApi {
    val taskQueue = Channel<suspend () -> PostResponse>(2)
    private val loger: Logger = LoggerFactory.getLogger(this.javaClass)

    //数据Api接口
    private fun build(url: String): PostResponse = runBlocking {
        launch {
            taskQueue.send {
                runBlocking {
                    try {
                        val request = Request.Builder()
                            .url(url)
                            .addHeader("Host", "api.tracker.gg")
                            .addHeader("Accept", "application/json")
                            .addHeader("user-agent", "Tracker Network App / 3.22.9")
                            .addHeader("x-app-version", "3.22.9")
                            .build()
                        //loger.info("正在执行:{}", url)
                        //.proxy(Proxy(Proxy.Type.HTTP, GatewayApi.sa))
                        val response = okHttpClient
                            .newBuilder()
                            .connectTimeout(15, TimeUnit.SECONDS)//设置连接超时时间
                            .readTimeout(15, TimeUnit.SECONDS)//设置读取超时时间
                            .build()
                            .newCall(request).execute()
                        if (response.isSuccessful) {
                            delay(800)
                            PostResponse(isSuccessful = true, reqBody = response.body.string())
                        } else {
                            delay(1000)
                            val reqBody = response.body.string()
                            loger.error("BTR最近战绩查询失败,{}", reqBody.replace("\n", ""))
                            PostResponse(isSuccessful = false, reqBody = reqBody)
                        }
                    } catch (ex: Exception) {
                        PostResponse(isSuccessful = false, error = ex.stackTraceToString())
                    }
                }
            }
        }
        taskQueue.receive().invoke()
    }

    fun recentlyServerSearch(eaid: String,times:Int= 0): MutableSet<BtrMatch>? {
        val matches = build("https://api.tracker.gg/api/v2/bf1/standard/matches/origin/${eaid}")
        //println(matches.reqBody.replace("\n","").substring(0,26))
        if (!matches.isSuccessful) return null
        val btrMatches = try {
            Gson().fromJson(matches.reqBody, BtrMatches::class.java)
        } catch (e: Exception) {
            loger.error("btr请求失败,{}", matches.reqBody)
            return null
        }
        val data: MutableSet<BtrMatch> = mutableSetOf()
        run p@{
            btrMatches.data.matches.forEachIndexed { index, it ->
                if (index > 2) return@p
                //val btrMatch = Cache.btrMatches[it.attributes.id] ?: Gson().fromJson(build("https://api.tracker.gg/api/v2/bf1/standard/matches/${it.attributes.id}").reqBody, BtrMatch::class.java)
                val btrMatch = Gson().fromJson(build("http://ipv6.ffshaozi.top:8080/btr/getMatch/${it.attributes.id}").reqBody, BtrMatch::class.java)
                if (btrMatch != null){
                    data.add(btrMatch)
                }
                runBlocking {
                    delay(1000)
                }
            }
        }
        return if (data.size == 0 && times < 5){
            runBlocking {
                delay(5000)
            }
            recentlyServerSearch(eaid, times + 1)
        }else{
            data
        }
    }
}