package data


/**
 * @Description
 * @Author littleArray
 * @Date 2023/10/16
 */
data class PlayerWeapons (
    var data: MutableList<Weapon> = mutableListOf(),
){
    data class Weapon(
        val name:String?=null,
        val type:String?=null,
        val accuracy: Double?=null,
        val destroyed: Int?=null,
        val headshots: Int?=null,
        val hits: Int?=null,
        val kills: Int?=null,
        val seconds: Double?=null,
        val shots: Int?=null
    )
}