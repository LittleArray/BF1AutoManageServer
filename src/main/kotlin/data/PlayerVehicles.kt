package data


/**
 * @Description
 * @Author littleArray
 * @Date 2023/10/16
 */
data class PlayerVehicles (
    var data: MutableList<Vehicle> = mutableListOf(),
){
    data class Vehicle(
        val name:String?=null,
        val type:String?=null,
        val destroyed: Double?=null,
        val kills: Double?=null,
        val seconds: Double?=null
    )
}