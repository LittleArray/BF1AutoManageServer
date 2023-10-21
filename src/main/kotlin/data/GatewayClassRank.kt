package data

data class GatewayClassRank(
    val id: String,
    val jsonrpc: String,
    val result: Result
) {
    data class Result(
        val assault: Assault,
        val cavalry: Cavalry,
        val medic: Medic,
        val pilot: Pilot,
        val scout: Scout,
        val support: Support,
        val tanker: Tanker
    ) {
        data class Assault(
            val actualValue: Double,
            val kitName: String,
            val rank: Int,
            val thresholds: List<Double>
        )

        data class Cavalry(
            val actualValue: Double,
            val kitName: String,
            val rank: Int,
            val thresholds: List<Double>
        )

        data class Medic(
            val actualValue: Double,
            val kitName: String,
            val rank: Int,
            val thresholds: List<Double>
        )

        data class Pilot(
            val actualValue: Double,
            val kitName: String,
            val rank: Int,
            val thresholds: List<Double>
        )

        data class Scout(
            val actualValue: Double,
            val kitName: String,
            val rank: Int,
            val thresholds: List<Double>
        )

        data class Support(
            val actualValue: Double,
            val kitName: String,
            val rank: Int,
            val thresholds: List<Double>
        )

        data class Tanker(
            val actualValue: Double,
            val kitName: String,
            val rank: Int,
            val thresholds: List<Double>
        )
    }
}