package api

import instance.Player
import instance.ServerInstance
import io.javalin.Javalin
import io.javalin.http.Context
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utils.ChineseTR.toTradition

/**
 * @Description
 * @Author littleArray
 * @Date 2023/11/5
 */
object QQBotApi {
    val app = Javalin.create(/*config*/)
    private val loger: Logger = LoggerFactory.getLogger(this.javaClass)



    fun init(port: Int) {
        app.start(port)
        app.get("/{gameid}/{method}/{param}") { ctx ->
            listen(ctx)
        }
    }
    data class Param(
        val gameID: Long,
        val token:String,
        val param: List<String>,
    )
    private fun listen(ctx: Context) {
        val gameid = try {
            ctx.pathParam("gameid").toLong()
        } catch (e: Exception) {
            ctx.result("非法GameID")
            return
        }
        val method = ctx.pathParam("method")
        val header = ctx.headerMap()
        val qqGroup = header["qqGroup"]
        val qqID = header["qq"]
        val token = header["token"]
        if (token == null|| qqID == null){
            ctx.result( "请求头参数不足")
            return
        }
        val param = Param(gameid, token,ctx.pathParam("param").split(","))
        if(qqID != "10086")
            loger.info("接收到来自QQ群的管服请求:{} 操作人:{} 操作token:{}",qqGroup,qqID,token)
        when (method) {
            "awl" -> awl(ctx,param)
            "rwl" -> awl(ctx,param)
            "kick" -> kick(ctx, param)
            "obscureKick" -> obscureKick(ctx, param)
            "obscureMove" -> obscureMove(ctx, param)
            "boom" -> boom(ctx, param)
            "ban" -> ban(ctx, param)
            "rban" -> rban(ctx, param)
            "vban" -> vban(ctx, param)
            "rvban" -> rvban(ctx, param)
            "aVip" -> aVip(ctx, param)
            "rVip" -> rVip(ctx, param)
            "getMaps" -> getMaps(ctx, param)
            "getLRClogs" -> getLRClogs(ctx, param)
            "chooseMap" -> chooseMap(ctx, param)
            else -> {
                ctx.result("无效的方法名")
            }
        }
    }

    private fun awl(ctx: Context, param: Param) {
        //ID
        if (param.param.isEmpty()) {
            ctx.result( "参数不足")
            return
        }
        val wlID = param.param[0]
        loger.info("申请加白名单 {} 服务器为 {}",wlID,param.gameID)
        val opServer = ServerInstance.getOpServer(param.token, param.gameID)
        if (opServer == null){
            ctx.result( "找不到服务器或token无效")
        }else{
            ctx.result(opServer.serverSetting.whitelist.add(wlID).toString())
        }
    }
    private fun rwl(ctx: Context, param: Param) {
        //ID
        if (param.param.isEmpty()) {
            ctx.result( "参数不足")
            return
        }
        val wlID = param.param[0]
        loger.info("申请移除白名单 {} 服务器为 {}",wlID,param.gameID)
        val opServer = ServerInstance.getOpServer(param.token, param.gameID)
        if (opServer == null){
            ctx.result( "找不到服务器或token无效")
        }else{
            ctx.result(opServer.serverSetting.whitelist.remove(wlID).toString())
        }
    }

    private fun kick(ctx: Context, param:Param) {
        //ID,原因?,cd?
        if (param.param.isEmpty()) {
            ctx.result( "参数不足")
            return
        }
        val kickID = param.param[0]
        val reason = param.param.getOrNull(1) ?: "RULEVIOLATION"
        val kickCD = try {
            param.param.getOrNull(2)?.toInt() ?: 0
        } catch (e: Exception) {
            0
        }
        loger.info("申请踢出 {} 理由:{} CD:{} 服务器为 {}", kickID, reason, kickCD,param.gameID)
        val opServer = ServerInstance.getOpServer(param.token, param.gameID)
        if (opServer == null){
            ctx.result( "找不到服务器或token无效")
        }else{
            var isKick = false
            opServer.playerList.forEach {
                if (it._p.NAME == kickID){
                    isKick = true
                    it.kick(reason, kickCD)
                    ctx.result("踢出 $kickID 成功")
                }
            }
            if (!isKick)
                ctx.result( "找不玩家")
        }

    }
    private fun obscureKick(ctx: Context, param:Param) {
        //ID,原因?,cd?
        if (param.param.isEmpty()) {
            ctx.result( "参数不足")
            return
        }
        val kickID = param.param[0]
        val reason = param.param.getOrNull(1) ?: "RULEVIOLATION"
        val kickCD = try {
            param.param.getOrNull(2)?.toInt() ?: 0
        } catch (e: Exception) {
            0
        }
        loger.info("申请模糊踢出 {} 理由:{} CD:{} 服务器为 {}", kickID, reason, kickCD,param.gameID)
        val opServer = ServerInstance.getOpServer(param.token, param.gameID)
        if (opServer == null){
            ctx.result( "找不到服务器或token无效")
        }else{
            val kickList = mutableListOf<Player>()
            opServer.playerList.forEach {
                if (it._p.NAME.indexOf(kickID,0,true) != -1 ){
                    kickList.add(it)
                }
            }
            when(kickList.size){
                0 -> {
                    ctx.result( "找不玩家")
                }
                1 -> {
                    kickList.first().kick(reason, kickCD)
                    ctx.result( "踢出 ${kickList.first()._p.NAME} 成功  理由:${reason}")
                }
                else ->{
                    ctx.result("包含多个玩家 ${kickList.map { it._p.NAME }}")
                }
            }

        }

    }
    private fun obscureMove(ctx: Context, param:Param) {
        //ID,原因?,cd?
        if (param.param.isEmpty()) {
            ctx.result( "参数不足")
            return
        }
        val id = param.param[0]
        loger.info("申请模糊换边 {} 服务器为 {}", id,param.gameID)
        val opServer = ServerInstance.getOpServer(param.token, param.gameID)
        if (opServer == null){
            ctx.result( "找不到服务器或token无效")
        }else{
            val moveList = mutableListOf<Player>()
            opServer.playerList.forEach {
                if (it._p.NAME.indexOf(id,0,true) != -1 ){
                    moveList.add(it)
                }
            }
            when(moveList.size){
                0 -> {
                    ctx.result( "找不玩家")
                }
                1 -> {
                    val moveTeam = if (moveList.first()._p.TIDX == 0L) 1 else 0
                    opServer.movePlayer(moveList.first().pid,moveTeam)
                    ctx.result( "移动 ${moveList.first()._p.NAME} 成功")
                }
                else ->{
                    ctx.result("包含多个玩家 ${moveList.map { it._p.NAME }}")
                }
            }

        }

    }

    private fun boom(ctx: Context, param: Param) {
        //理由?
        val reason = param.param.getOrNull(0) ?: "Boom!"
        loger.info("申请炸服{} 理由:{}", param.gameID, reason)
        val opServer = ServerInstance.getOpServer(param.token, param.gameID)
        if (opServer != null){
            opServer.playerList.forEach {
                it.kick(reason)
            }
            ctx.result( "炸服成功 共踢出${opServer.playerList.size}个玩家")
        }else{
            ctx.result( "找不到服务器或token无效")
        }
    }

    private fun ban(ctx: Context, param:Param) {
        //ID
        if (param.param.isEmpty()) {
            ctx.result( "参数不足")
            return
        }
        val banID = param.param[0]
        loger.info("申请Ban {} 服务器为 {}", banID, param.gameID)
        val opServer = ServerInstance.getOpServer(param.token, param.gameID)
        if (opServer != null){
            ctx.result( "玩家 $banID 封禁状态为:${opServer.addBan(banID)}")
        }else{
            ctx.result( "找不到服务器")
        }
    }
    private fun rban(ctx: Context, param:Param) {
        //PID
        if (param.param.isEmpty()) {
            ctx.result( "参数不足")
            return
        }
        val pid = param.param[0]
        loger.info("申请removeBan {} 服务器为 {}", pid, param.gameID)
        val opServer = ServerInstance.getOpServer(param.token, param.gameID)
        if (opServer != null){
            ctx.result( opServer.removeBan(pid).toString())
        }else{
            ctx.result( "找不到服务器或token无效")
        }
    }
    private fun vban(ctx: Context, param:Param) {
        //ID
        if (param.param.isEmpty()) {
            ctx.result( "参数不足")
            return
        }
        val banID = param.param[0]
        loger.info("申请VBan {} 服务器为 {}", banID, param.gameID)
        val opServer = ServerInstance.getOpServer(param.token, param.gameID)
        if (opServer != null){
            ctx.result( opServer.addVBan(banID).toString())
        }else{
            ctx.result( "找不到服务器或token无效")
        }
    }
    private fun rvban(ctx: Context, param:Param) {
        //ID
        if (param.param.isEmpty()) {
            ctx.result( "参数不足")
            return
        }
        val banID = param.param[0]
        loger.info("申请removeVBan {} 服务器为 {}", banID, param.gameID)
        val opServer = ServerInstance.getOpServer(param.token, param.gameID)
        if (opServer != null){
            ctx.result( opServer.removeVban(banID).toString())
        }else{
            ctx.result( "找不到服务器或token无效")
        }
    }

    private fun aVip(ctx: Context, param: Param) {
        //ID
        if (param.param.isEmpty()) {
            ctx.result( "参数不足")
            return
        }

        val id = param.param[0]
        loger.info("申请添加vip {} 服务器为 {}",  id, param.gameID)
        val opServer = ServerInstance.getOpServer(param.token, param.gameID)
        if (opServer != null){
            ctx.result( opServer.addVip(id).toString())
        }else{
            ctx.result( "找不到服务器或token无效")
        }
    }

    private fun rVip(ctx: Context, param: Param) {
        //PID
        if (param.param.isEmpty()) {
            ctx.result( "参数不足")
            return
        }
        val pid = param.param[0]
        loger.info("申请移除vip {} 服务器为 {}", pid, param.gameID)
        val opServer = ServerInstance.getOpServer(param.token, param.gameID)
        if (opServer != null){
            ctx.result( opServer.removeVip(pid).toString())
        }else{
            ctx.result( "找不到服务器或token无效")
        }
    }

    private fun getMaps(ctx: Context, param: Param) {
        loger.info("获取图池 服务器为 {}",param.gameID)
        val opServer = ServerInstance.getOpServer(param.token, param.gameID)
        if (opServer != null){
            ctx.json( object {
                    val maps = opServer.getMap()
                    val nowMap = opServer.getRSPInfo()?.serverInfo?.mapNamePretty
                }
            )
        }else{
            ctx.result( "找不到服务器或token无效")
        }
    }
    private fun getLRClogs(ctx: Context, param: Param) {
        //loger.info("获取LRClogs 服务器为 {}",param.gameID)
        val opServer = ServerInstance.getOpServer(param.token, param.gameID)
        if (opServer != null){
            opServer.lrcLog?.let {
                ctx.json(it)
            }
        }else{
            ctx.result( "找不到服务器或token无效")
        }
    }

    private fun chooseMap(ctx: Context,param: Param) {
        //图池ID
        if (param.param.isEmpty()) {
            ctx.result( "参数不足")
            return
        }
        val mapName = param.param[0].toTradition()
        loger.info("切图:{} 服务器为 {}", mapName, param.gameID)
        val opServer = ServerInstance.getOpServer(param.token, param.gameID)
        if (opServer != null){
            var index = -1
            opServer.getMap()?.forEachIndexed { INDEX, it ->
                if (it.contains(mapName.toTradition()) )
                    index = INDEX
            }
            ctx.json( opServer.chooseMap(index))
        }else{
            ctx.result( "找不到服务器或token无效")
        }
    }
}