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
    launch {
        taskQueue.send { "Task 1" }
        taskQueue.send {
            runBlocking {
                delay(1000)
                "Task 2"
            }
        }
        taskQueue.send { "Task 3" }
    }
    println(taskQueue.receive().invoke())
    println(taskQueue.receive().invoke())
    println(taskQueue.receive().invoke())
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