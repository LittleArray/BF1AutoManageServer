package data

import java.util.*

/**
 * @Description
 * @Author littleArray
 * @Date 2023/10/7
 */
data class PostResponse(
    var isSuccessful: Boolean = false,
    var error: String = "",
    var reqBody: String = "",
)

data class JsonRpcObj(
    val jsonrpc: String = "2.0",
    var method: String? = "",
    var params: Any? = "",
    val id: String = UUID.randomUUID().toString()
)