package data

data class GatewayServerSearch(
    val id: String,
    val jsonrpc: String,
    val result: Result
) {
    data class Result(
        val gameservers: List<Gameserver>,
        val hasMoreResults: Boolean
    ) {
        data class Gameserver(
            val country: String,
            val custom: Boolean,
            val description: String,
            val expansions: List<Expansion>,
            val experience: String,
            val game: String,
            val gameId: String,
            val guid: String,
            val ip: String,
            val isFavorite: Boolean,
            val mapExpansion: MapExpansion,
            val mapImageUrl: String,
            val mapMode: String,
            val mapModePretty: String,
            val mapName: String,
            val mapNamePretty: String,
            val mapRotation: List<Any>,
            val mixId: Any,
            val name: String,
            val officialExperienceId: String,
            val operationIndex: Int,
            val overallGameMode: Any,
            val ownerId: Any,
            val passwordProtected: Boolean,
            val pingSiteAlias: String,
            val platform: String,
            val playgroundId: Any,
            val preset: String,
            val protocolVersion: String,
            val ranked: Boolean,
            val region: String,
            val secret: String,
            val serverMode: Any,
            val serverType: String,
            val settings: Settings,
            val slots: Slots,
            val tickRate: Int
        ) {
            data class Expansion(
                val license: String,
                val mask: Int,
                val name: String,
                val prettyName: String
            )

            data class MapExpansion(
                val license: String,
                val mask: Int,
                val name: String,
                val prettyName: String
            )

            class Settings

            data class Slots(
                val Queue: Queue_,
                val Soldier: Soldier_,
                val Spectator: Spectator_
            ) {
                data class Queue_(
                    val current: Int,
                    val max: Int
                )

                data class Soldier_(
                    val current: Int,
                    val max: Int
                )

                data class Spectator_(
                    val current: Int,
                    val max: Int
                )
            }
        }
    }
}