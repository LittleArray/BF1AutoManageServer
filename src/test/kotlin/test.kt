import api.GatewayApi
import com.charleskorn.kaml.Yaml
import instance.Server
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import utils.DataUtils
import java.util.*
import kotlin.reflect.KProperty0

/**
 * @Description
 * @Author littleArray
 * @Date 2023/10/21
 */
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

val taskQueue = Channel<suspend () -> String>(1)

fun main() = runBlocking {
    println(countIL("EvillIIlIlIl"))
}
//条形码检测
//EvillIIlIlIl
fun countIL(str: String): Int {
    var count = 0 // 记录连续出现的 "I" 和 "l" 的总次数
    var index = 0 // 当前遍历的字符索引

    while (index < str.length - 1) { // 注意：遍历索引不能超过倒数第二个字符
        val currentChar = str[index].toString()
        val nextChar = str[index + 1].toString()
        if ((currentChar == "I" || currentChar == "l") && (nextChar == "I" || nextChar == "l")) {
            count++
        }else{
            val spString = str.substring(index + 1)
            val nextCountIL = countIL(spString)
            return if (nextCountIL > count) nextCountIL else count
        }

        index++
    }

    return count
}
@Serializable
data class Test(
    var rspId: Long = 0,
    var token: String = UUID.randomUUID().toString(),
    var gameId: Long = 0,
    var sessionID: String = "",
    var sid: String = "",
    var remid: String = "",
    var lifeMaxKD: Double = 5.0,
    var lifeMaxKPM: Double = 5.0,
    var lifeMaxKPM95: Double = 5.0,
    var lifeMaxKD95: Double = 5.0,
    var lifeMaxKPM150: Double = 5.0,
    var lifeMaxKD150: Double = 5.0,
    var recMaxKPM: Double = 5.0,
    var recMaxKD: Double = 5.0,
    var kickCD: Int = 0,
    var killsLimited: Int = 999,
    var matchKillsEnable: Int = 999,
    var matchKDLimited: Double = 5.0,
    var winPercentLimited: Double = 1.1,
    var rankLimited: Int = 151,
    var reEnterKick: Boolean = false,
    var spectatorKick: Boolean = false,
    var classRankLimited: MutableMap<String, Int> = mutableMapOf(
        Pair("assault",51),
        Pair("cavalry",51),
        Pair("medic",51),
        Pair("pilot",51),
        Pair("tanker",51),
    ),
    var weaponLimited: MutableList<String> = mutableListOf(),
    var weaponStarLimited: Int = 9999,
    var vehicleLimited: MutableList<String> = mutableListOf(),
    var vehicleStarLimited: Int = 9999,
    var platoonLimited: MutableList<String> = mutableListOf(),
    var whitelist: MutableList<String> = mutableListOf(),
    var botlist: MutableList<String> = mutableListOf(),
    var vbanlist: MutableList<String> = mutableListOf(),
    var adminlist: MutableList<String> = mutableListOf()
)
class b(){
    var a = 1243434
    fun main(){
        poo(this::a)
        Thread.sleep(1000)
        a = 662
        Thread.sleep(1000)
        a = 6622
        Thread.sleep(3000)
        a = 66243
    }
}
fun poo(kProperty0: KProperty0<Int>) {
    Thread{
        while (true){
            println(kProperty0.get())
            Thread.sleep(2000)
        }
    }.start()

}