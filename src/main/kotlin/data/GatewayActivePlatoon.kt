package data

data class GatewayActivePlatoon(
    val id: String,
    val jsonrpc: String,
    val result: Result?=null
) {
    data class Result(
        val creatorId: String,
        val dateCreated: Long,
        val description: String,
        val emblem: String,
        val guid: String,
        val joinConfig: JoinConfig,
        val name: String,
        val size: Int,
        val tag: String,
        val verified: Boolean
    ) {
        data class JoinConfig(
            val canApplyMembership: Boolean,
            val isFreeJoin: Boolean
        )
    }
}