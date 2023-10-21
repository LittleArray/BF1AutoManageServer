package data

data class GatewaySessionId(
    val id: String,
    val jsonrpc: String,
    val result: Result
) {
    data class Result(
        val envId: String,
        val parameters: Parameters,
        val personaId: String,
        val sessionId: String
    ) {
        data class Parameters(
            val background: Any,
            val bbPrefix: String,
            val currentUtcTimestamp: String,
            val featureFlags: List<String>,
            val hasOnlineAccess: Boolean,
            val supportsCampaignOperations: Boolean,
            val supportsFilterState: Boolean
        )
    }
}