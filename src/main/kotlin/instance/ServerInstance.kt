package instance

import utils.DataUtils

/**
 * @Description
 * @Author littleArray
 * @Date 2023/10/21
 */
object ServerInstance {
    var INSTANCE = mutableListOf<Server>()
    fun addServer(serverSetting: Server.ServerSetting): Boolean {
        if (INSTANCE.any{it.serverSetting.gameId == serverSetting.gameId}) {
            return false
        }
        return INSTANCE.add(Server(serverSetting))
    }

    fun removeServer(gameID: Long): Boolean {
        return INSTANCE.removeIf { it.serverSetting.gameId == gameID }
    }
    fun getOpServer(token:String,gameID: Long): Server? {
        INSTANCE.forEach {
            if (it.serverSetting.gameId == gameID && it.serverSetting.token == token){
                return it
            }
        }
        return null
    }
    fun save() {
        var content = ""
        INSTANCE.forEach {
            it.saveServer()
            content += it.serverSetting.gameId.toString() + ","
        }
        DataUtils.save("INSTANCE", content)
    }

    fun load() {
        val content = DataUtils.load("INSTANCE")
        content.split(",").forEach {
            if (it.isNotEmpty())
                INSTANCE.add(Server(Server.ServerSetting(gameId = it.toLong())))
        }
        INSTANCE.forEach {
            it.loadServer()
        }
    }
}