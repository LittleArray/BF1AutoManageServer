package api

import api.GatewayApi.okHttpClient
import com.google.gson.Gson
import config.GConfig
import data.BtrMatch
import data.BtrMatches
import data.PostResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
    fun getRec(eaid: String) = build("https://api.tracker.gg/api/v2/bf1/standard/matches/origin/${eaid}")
    fun recentlyServerSearch(eaid: String, pid: String, count: Double,times:Int = 0): MutableSet<BtrMatch.Data.Segment> {
        var data: MutableSet<BtrMatch.Data.Segment> = mutableSetOf()
        val matches = build("https://api.tracker.gg/api/v2/bf1/standard/matches/origin/${eaid}")
        //println(matches.reqBody.replace("\n","").substring(0,26))
        if (!matches.isSuccessful) return data
        if (matches.reqBody.isEmpty()) return data
        val btrMatches = try {
            Gson().fromJson(matches.reqBody, BtrMatches::class.java)
        } catch (e: Exception) {
            loger.error("btr请求失败,{}", matches.reqBody)
            return data
        }
        var index = 0
        run p@{
            btrMatches.data.matches.forEach {
                if (index > count) return@p
                //val btrMatch = Cache.btrMatches[it.attributes.id] ?: Gson().fromJson(build("https://api.tracker.gg/api/v2/bf1/standard/matches/${it.attributes.id}").reqBody, BtrMatch::class.java)
                val btrMatch = try {
                    Gson().fromJson(
                        build("${GConfig.Config.serverUrl}/btr/getMatch/${it.attributes.id}").reqBody,
                        BtrMatch::class.java
                    )
                } catch (e: Exception) {
                    return@p
                }
                if (btrMatch != null) {
                    run {
                        if (btrMatch.data != null)
                            btrMatch.data.segments?.forEach {
                                if (it != null)
                                    if (it.attributes?.playerId == pid && it.type == "player") {
                                        val ntime = it.stats?.time?.value?.div(60) ?: 0.0
                                        val nkills = (it.stats?.kills?.value?.toInt() ?: 0) + (it.stats?.killsAssistAsKills?.value?.toInt() ?:0)
                                        if (ntime > 5 && nkills > 0) {
                                            data.add(it)
                                            index++
                                        }
                                        return@run
                                    }
                            }
                    }
                }
                runBlocking {
                    delay(1000)
                }
            }
        }
        if (data.isEmpty() && times < 5)
            data = recentlyServerSearch(eaid, pid, count,times+1)
        return data
    }
}