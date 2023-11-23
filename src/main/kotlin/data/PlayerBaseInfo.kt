package data

/**
 * @Description
 * @Author littleArray
 * @Date 2023/10/8
 */
data class PlayerBaseInfo(
    var pid: Long = 0L,
    var rank: Int = 0,
    var kills: Int = 0,
    var deaths: Int = 0,
    var kpm: Double = 0.0,
    var kd: Double = 0.0,
    var spm: Double = 0.0,
    var skill: Double = 0.0,
    var accuracy: Double = 0.0,
    var highestKillStreak: Int = 0,
    var longestHeadShot: Double = 0.0,
    var headShots: Int = 0,
    var dogtagsTaken: Int = 0,
    var avengerKills: Int = 0,
    var saviorKills: Int = 0,
    var killAssists: Double = 0.0,
    var revives: Double = 0.0,
    var heals: Double = 0.0,
    var repairs: Double = 0.0,
    var wins: Int = 0,
    var losses: Int = 0,
    var timePlayed: Int = 0,
    var avatarImageUrl: String = "",
    var bestClass: String = "",
    var activePlatoon: Platoon = Platoon(),
    var platoons: MutableList<Platoon> = mutableListOf(),
    var gamemodes: MutableList<Gamemode> = mutableListOf(),
    var classes: MutableList<Classes> = mutableListOf(),
    var nowPlayed:NowPlayed = NowPlayed(),
    var recPlayed:MutableList<NowPlayed> = mutableListOf(),
    var topVehicles:PlayerVehicles = PlayerVehicles(),
    var topWeapons:PlayerWeapons = PlayerWeapons(),
) {
    data class NowPlayed(
        var serverName:String="",
        var gameId:String=""
    )

    data class Platoon(
        var name: String?=null,
        var description: String?=null,
        var tag: String?=null,
        var emblem: String?=null,
        var verified: Boolean?=null,
        var dateCreated: Long?=null,
        var creatorId: Long?=null,
        var size: Int?=null,
        var id: String?=null
    )

    data class Gamemode(
        var modeName: String,
        var losses: Int,
        var score: Double,
        var winPercent: Double,
        var wins: Int
    )

    data class Classes(
        var className: String,
        var classRank: Int=0,
        var secondsPlayed: Double,
        var kills: Double,
        var score: Double,
    )
}