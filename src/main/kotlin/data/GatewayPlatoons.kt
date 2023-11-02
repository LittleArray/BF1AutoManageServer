package data

data class GatewayPlatoons(
    val id: String,
    val jsonrpc: String,
    val result: List<Result>
) {
    data class Result(
        val creatorId: String,
        val dateCreated: Long,
        val description: String,
        val emblem: String?=null,
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