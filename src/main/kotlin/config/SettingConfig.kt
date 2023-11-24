package config

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.Serializable

@Serializable
data class SettingConfig (
    @YamlComment("获取数据的服务器地址","如果有ipv6网路请填入:http://ipv6.ffshaozi.top:8080","如果没有ipv6或者无法访问请填入:http://ffshaozi.top:8080")
    var serverUrl: String = "http://ffshaozi.top:8080",
    @YamlComment("网路本地代理","为空不启用")
    var proxies: String = "",
    @YamlComment("网路代理端口","不用代理别动,没有判空处理")
    var port: Int = 7890
)
