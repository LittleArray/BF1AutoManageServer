package utils

import com.spreada.utils.chinese.ZHConverter

/**
 * @Description
 * @Author littleArray
 * @Date 2023/10/19
 */
object ChineseTR {
    fun String.toTradition():String = ZHConverter.convert(this,0)
    fun String.toSimplified():String = ZHConverter.convert(this,1)
}