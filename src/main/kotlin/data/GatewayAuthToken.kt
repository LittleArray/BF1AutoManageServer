package data

data class GatewayAuthToken(
    val access_token: String,
    val expires_in: String,
    val token_type: String,
    val sid:String
)