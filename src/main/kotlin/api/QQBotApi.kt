package api

import instance.Player
import instance.ServerInstance
import io.javalin.Javalin
import io.javalin.http.Context
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
    data class Res(
        val code: ErrCode,
        val data: Any?=null,
    )
    enum class ErrCode {
        OK,
        NOT_FOUND,
        PARAMETERS_ARE_MISSING,
        HEADER_PARAMETERS_ARE_MISSING,
        ILLEGAL_PARAMETERS,
        INVALID_METHOD,
        TOO_MANY_INSTANCE,
    }
    private fun listen(ctx: Context) {
        val gameid = try {
            ctx.pathParam("gameid").toLong()
        } catch (e: Exception) {
            ctx.json(Res(ErrCode.ILLEGAL_PARAMETERS, "非法GameID"))
            return
        }
        val method = ctx.pathParam("method")
        val header = ctx.headerMap()
        val qqGroup = header["qqGroup"]
        val qqID = header["qq"]
        val token = header["token"]
        if (token == null|| qqID == null){
            ctx.json(Res(ErrCode.HEADER_PARAMETERS_ARE_MISSING, "请求头参数不足"))
            return
        }
        val param = Param(gameid, token,ctx.pathParam("param").split(","))
        loger.info("接收到来自QQ群的管服请求:{} 操作人:{} 操作token:{}",qqGroup,qqID,token)
        when (method) {
            "awl" -> awl(ctx,param)
            "rwl" -> awl(ctx,param)
            "kick" -> kick(ctx, param)
            "obscureKick" -> obscureKick(ctx, param)
            "boom" -> boom(ctx, param)
            "ban" -> ban(ctx, param)
            "vban" -> vban(ctx, param)
            "rvban" -> rvban(ctx, param)
            "aVip" -> aVip(ctx, param)
            "rVip" -> rVip(ctx, param)
            "getMap" -> getMap(ctx, param)
            "chooseMap" -> chooseMap(ctx, param)
            else -> {
                ctx.json(Res(ErrCode.INVALID_METHOD, "无效的方法名"))
            }
        }
    }

    private fun awl(ctx: Context, param: Param) {
        //ID
        if (param.param.isEmpty()) {
            ctx.json(Res(ErrCode.PARAMETERS_ARE_MISSING, "参数不足"))
            return
        }
        val wlID = param.param[0]
        loger.info("申请加白名单 {} 服务器为 {}",wlID,param.gameID)
        val opServer = ServerInstance.getOpServer(param.token, param.gameID)
        if (opServer == null){
            ctx.json(Res(ErrCode.NOT_FOUND, "找不到服务器或token无效"))
        }else{
            ctx.json(Res(ErrCode.OK,opServer.serverSetting.whitelist.add(wlID)))
        }
    }
    private fun rwl(ctx: Context, param: Param) {
        //ID
        if (param.param.isEmpty()) {
            ctx.json(Res(ErrCode.PARAMETERS_ARE_MISSING, "参数不足"))
            return
        }
        val wlID = param.param[0]
        loger.info("申请移除白名单 {} 服务器为 {}",wlID,param.gameID)
        val opServer = ServerInstance.getOpServer(param.token, param.gameID)
        if (opServer == null){
            ctx.json(Res(ErrCode.NOT_FOUND, "找不到服务器或token无效"))
        }else{
            ctx.json(Res(ErrCode.OK,opServer.serverSetting.whitelist.remove(wlID)))
        }
    }

    private fun kick(ctx: Context, param:Param) {
        //ID,原因?,cd?
        if (param.param.isEmpty()) {
            ctx.json(Res(ErrCode.PARAMETERS_ARE_MISSING, "参数不足"))
            return
        }
        val kickID = param.param[0]
        val reason = param.param.getOrNull(1) ?: "Kick Without Reason"
        val kickCD = try {
            param.param.getOrNull(2)?.toInt() ?: 0
        } catch (e: Exception) {
            0
        }
        loger.info("申请踢出 {} 理由:{} CD:{} 服务器为 {}", kickID, reason, kickCD,param.gameID)
        val opServer = ServerInstance.getOpServer(param.token, param.gameID)
        if (opServer == null){
            ctx.json(Res(ErrCode.NOT_FOUND, "找不到服务器或token无效"))
        }else{
            var isKick = false
            opServer.playerList.forEach {
                if (it._p.NAME == kickID){
                    isKick = true
                    it.kick(reason, kickCD)
                    ctx.json(Res(ErrCode.OK, {
                        val NAME = kickID
                        val COOLDOWN = kickCD
                        val REASON = reason
                    }))
                }
            }
            if (!isKick)
                ctx.json(Res(ErrCode.NOT_FOUND, "找不玩家"))
        }

    }
    private fun obscureKick(ctx: Context, param:Param) {
        //ID,原因?,cd?
        if (param.param.isEmpty()) {
            ctx.json(Res(ErrCode.PARAMETERS_ARE_MISSING, "参数不足"))
            return
        }
        val kickID = param.param[0]
        val reason = param.param.getOrNull(1) ?: "Kick Without Reason"
        val kickCD = try {
            param.param.getOrNull(2)?.toInt() ?: 0
        } catch (e: Exception) {
            0
        }
        loger.info("申请模糊踢出 {} 理由:{} CD:{} 服务器为 {}", kickID, reason, kickCD,param.gameID)
        val opServer = ServerInstance.getOpServer(param.token, param.gameID)
        if (opServer == null){
            ctx.json(Res(ErrCode.NOT_FOUND, "找不到服务器或token无效"))
        }else{
            val kickList = mutableListOf<Player>()
            opServer.playerList.forEach {
                if (it._p.NAME.indexOf(kickID,0,true) != -1 ){
                    kickList.add(it)
                }
            }
            when(kickList.size){
                0 -> {
                    ctx.json(Res(ErrCode.NOT_FOUND, "找不玩家"))
                }
                1 -> {
                    kickList.first().kick(reason, kickCD)
                    ctx.json(Res(ErrCode.OK, "踢出成功${kickID} 理由:${reason}"))
                }
                else ->{
                    kickList.first().kick(reason, kickCD)
                    ctx.json(Res(ErrCode.TOO_MANY_INSTANCE, kickList.map { it._p.NAME }))
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
            ctx.json(Res(ErrCode.OK, "炸服成功 共踢出${opServer.playerList.size}个玩家"))
        }else{
            ctx.json(Res(ErrCode.NOT_FOUND, "找不到服务器或token无效"))
        }
    }

    private fun ban(ctx: Context, param:Param) {
        //ID
        if (param.param.isEmpty()) {
            ctx.json(Res(ErrCode.PARAMETERS_ARE_MISSING, "参数不足"))
            return
        }
        val banID = param.param[0]
        loger.info("申请Ban {} 服务器为 {}", banID, param.gameID)
        val opServer = ServerInstance.getOpServer(param.token, param.gameID)
        if (opServer != null){
            ctx.json(Res(ErrCode.OK, opServer.addBan(banID)))
        }else{
            ctx.json(Res(ErrCode.NOT_FOUND, "找不到服务器"))
        }
    }
    private fun rban(ctx: Context, param:Param) {
        //PID
        if (param.param.isEmpty()) {
            ctx.json(Res(ErrCode.PARAMETERS_ARE_MISSING, "参数不足"))
            return
        }
        val pid = param.param[0]
        loger.info("申请removeBan {} 服务器为 {}", pid, param.gameID)
        val opServer = ServerInstance.getOpServer(param.token, param.gameID)
        if (opServer != null){
            ctx.json(Res(ErrCode.OK, opServer.removeBan(pid)))
        }else{
            ctx.json(Res(ErrCode.NOT_FOUND, "找不到服务器或token无效"))
        }
    }
    private fun vban(ctx: Context, param:Param) {
        //ID
        if (param.param.isEmpty()) {
            ctx.json(Res(ErrCode.PARAMETERS_ARE_MISSING, "参数不足"))
            return
        }
        val banID = param.param[0]
        loger.info("申请VBan {} 服务器为 {}", banID, param.gameID)
        val opServer = ServerInstance.getOpServer(param.token, param.gameID)
        if (opServer != null){
            ctx.json(Res(ErrCode.OK, opServer.addVBan(banID)))
        }else{
            ctx.json(Res(ErrCode.NOT_FOUND, "找不到服务器或token无效"))
        }
    }
    private fun rvban(ctx: Context, param:Param) {
        //ID
        if (param.param.isEmpty()) {
            ctx.json(Res(ErrCode.PARAMETERS_ARE_MISSING, "参数不足"))
            return
        }
        val banID = param.param[0]
        loger.info("申请removeVBan {} 服务器为 {}", banID, param.gameID)
        val opServer = ServerInstance.getOpServer(param.token, param.gameID)
        if (opServer != null){
            ctx.json(Res(ErrCode.OK, opServer.removeVban(banID)))
        }else{
            ctx.json(Res(ErrCode.NOT_FOUND, "找不到服务器或token无效"))
        }
    }

    private fun aVip(ctx: Context, param: Param) {
        //ID
        if (param.param.isEmpty()) {
            ctx.json(Res(ErrCode.PARAMETERS_ARE_MISSING, "参数不足"))
            return
        }

        val id = param.param[0]
        loger.info("申请添加vip {} 服务器为 {}",  id, param.gameID)
        val opServer = ServerInstance.getOpServer(param.token, param.gameID)
        if (opServer != null){
            ctx.json(Res(ErrCode.OK, opServer.addVip(id)))
        }else{
            ctx.json(Res(ErrCode.NOT_FOUND, "找不到服务器或token无效"))
        }
    }

    private fun rVip(ctx: Context, param: Param) {
        //PID
        if (param.param.size < 3) {
            ctx.json(Res(ErrCode.PARAMETERS_ARE_MISSING, "参数不足"))
            return
        }
        val pid = param.param[0]
        loger.info("申请添加vip {} 服务器为 {}", pid, param.gameID)
        val opServer = ServerInstance.getOpServer(param.token, param.gameID)
        if (opServer != null){
            ctx.json(Res(ErrCode.OK, opServer.removeVip(pid)))
        }else{
            ctx.json(Res(ErrCode.NOT_FOUND, "找不到服务器或token无效"))
        }
    }

    private fun getMap(ctx: Context, param: Param) {
        loger.info("获取图池 服务器为 {}",param.gameID)
        val opServer = ServerInstance.getOpServer(param.token, param.gameID)
        if (opServer != null){
            ctx.json(Res(ErrCode.OK, opServer.getMap()))
        }else{
            ctx.json(Res(ErrCode.NOT_FOUND, "找不到服务器或token无效"))
        }
    }

    private fun chooseMap(ctx: Context,param: Param) {
        //图池ID
        if (param.param.isEmpty()) {
            ctx.json(Res(ErrCode.PARAMETERS_ARE_MISSING, "参数不足"))
            return
        }
        val index = try {
            param.param[0].toInt()
        } catch (e: Exception) {
            ctx.json(Res(ErrCode.ILLEGAL_PARAMETERS, "非法参数"))
            return
        }
        loger.info("切图:{} 服务器为 {}", index, param.gameID)
        val opServer = ServerInstance.getOpServer(param.token, param.gameID)
        if (opServer != null){
            ctx.json(Res(ErrCode.OK, opServer.chooseMap(index)))
        }else{
            ctx.json(Res(ErrCode.NOT_FOUND, "找不到服务器或token无效"))
        }
    }
}