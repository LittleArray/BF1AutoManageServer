package data

/**
 * @Description
 * @Author littleArray
 * @Date 2023/10/28
 */
object KitCache {
    var cache:MutableMap<String,MutableMap<Long,Int>> = mutableMapOf()//NAME -> PID -> KILLS
}