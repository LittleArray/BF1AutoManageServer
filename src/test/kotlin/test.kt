import api.GatewayApi
import kotlin.reflect.KProperty0

/**
 * @Description
 * @Author littleArray
 * @Date 2023/10/21
 */
fun main(){
   // println(GatewayApi.removeServerBan("3cc8e89b-04e8-40da-b15b-052059df698c", 11934453, "1006716545570"))
    println(Math.random() * 1000)
}
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