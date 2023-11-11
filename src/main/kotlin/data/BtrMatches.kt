package data

data class BtrMatches(
    val `data`: Data
) {
    data class Data(
        val expiryDate: String,
        val matches: List<Matche>,
        val metadata: Metadata,
        val paginationType: String,
        val requestingPlayerAttributes: RequestingPlayerAttributes
    ) {
        data class Matche(
            val attributes: Attributes,
            val expiryDate: String,
            val metadata: Metadata,
            val segments: List<Any>,
            val streams: Any
        ) {
            data class Attributes(
                val gamemodeKey: String,
                val id: String,
                val mapKey: String
            )

            data class Metadata(
                val gamemodeName: String,
                val isRanked: Boolean,
                val mapImageUrl: String,
                val mapName: String,
                val serverName: String,
                val timestamp: String
            )
        }

        data class Metadata(
            val next: String
        )

        data class RequestingPlayerAttributes(
            val platformId: Int,
            val platformUserIdentifier: String
        )
    }
}