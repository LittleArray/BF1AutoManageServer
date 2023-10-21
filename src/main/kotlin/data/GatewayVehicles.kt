package data

data class GatewayVehicles(
    val id: String,
    val jsonrpc: String,
    val result: List<Result>
) {
    data class Result(
        val accessories: List<Any>,
        val name: String,
        val sortOrder: Int,
        val star: Star,
        val stats: Stats,
        val vehicles: List<Vehicle>
    ) {
        data class Star(
            val imageUrl: String,
            val images: Any,
            val progression: Progression,
            val timesAquired: Int
        ) {
            data class Progression(
                val unlocked: Boolean,
                val valueAcquired: Double,
                val valueNeeded: Double
            )
        }

        data class Stats(
            val values: Values
        ) {
            data class Values(
                val destroyed: Double,
                val kills: Double,
                val seconds: Double
            )
        }

        data class Vehicle(
            val accessories: List<Any>,
            val description: String,
            val expansion: String,
            val guid: String,
            val imageUrl: String,
            val images: Images,
            val name: String,
            val progression: Progression,
            val rank: Any,
            val stats: Stats
        ) {
            data class Images(
                val Png256xANY: String,
                val Small: String
            )

            data class Progression(
                val unlocked: Boolean,
                val valueAcquired: Double,
                val valueNeeded: Double
            )

            data class Stats(
                val values: Values?=null
            ) {
                data class Values(
                    val destroyed: Double?=null,
                    val kills: Double?=null,
                    val seconds: Double?=null
                )
            }
        }
    }
}