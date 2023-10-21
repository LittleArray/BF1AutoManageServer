import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.reflect.KProperty0

/**
 * @Description
 * @Author littleArray
 * @Date 2023/10/21
 */
fun main(){
    b().main()
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