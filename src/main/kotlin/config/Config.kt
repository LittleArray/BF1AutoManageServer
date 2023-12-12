package config

import api.GatewayUtils
import com.charleskorn.kaml.Yaml
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utils.DataUtils
import java.net.InetSocketAddress
import java.net.SocketAddress

object Config {
    var Config = SettingConfig()
    var sa: SocketAddress? = null
    var loaderr = false
    private val loger: Logger = LoggerFactory.getLogger(this.javaClass)
    fun saveConfig() {
        if (loaderr) return
        try {
            DataUtils.save("Config", Yaml.default.encodeToString(SettingConfig.serializer(), Config))
            loger.info("配置保存成功")
        } catch (e: Exception) {
            loger.info("配置保存失败 {}", e.stackTraceToString())
        }
    }

    fun loadConfig() {
        try {
            val string = DataUtils.load("Config")
            if (string.isEmpty()) {
                saveConfig()
                loadConfig()
                loger.info("配置初始化成功")
            } else {
                val newConfig = Yaml.default.decodeFromString(SettingConfig.serializer(), string)
                Config = newConfig
                if (Config.proxies.isNotEmpty())
                    sa = InetSocketAddress(Config.proxies, Config.port)
                loger.info("配置载入成功")
                updateSessionID()
            }
        } catch (e: Exception) {
            loger.info("配置载入失败 {}", e.stackTraceToString())
            loaderr = true
        }
    }
    fun updateSessionID(){
        Config.oplist.forEach {
            CoroutineScope(Dispatchers.IO).launch {
                while (true){
                    delay(10 * 60 * 60 * 1000)
                    try {
                        it.sessionID = GatewayUtils.getSessionId(it.sid,it.remid)?:it.sessionID
                    } catch (e: Exception) {
                        TODO("更新sessionID失败")
                    }
                }
            }
        }
    }
}