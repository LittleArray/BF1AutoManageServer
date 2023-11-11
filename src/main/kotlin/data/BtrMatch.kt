package data
data class BtrMatch(
    val `data`: Data? = null
) {
    data class Data(
        val attributes: Attributes? = null,
        val expiryDate: String? = null,
        val metadata: Metadata? = null,
        val segments: List<Segment?>? = null,
        val streams: Any? = null
    ) {
        data class Attributes(
            val gamemodeKey: String? = null,
            val id: String? = null,
            val mapKey: String? = null
        )

        data class Metadata(
            val duration: Double? = null,
            val gamemodeName: String? = null,
            val isRanked: Boolean? = null,
            val mapImageUrl: String? = null,
            val mapName: String? = null,
            val maxClientCount: Double? = null,
            val mod: String? = null,
            val serverName: String? = null,
            val serverType: String? = null,
            val teams: List<Team?>? = null,
            val timestamp: String? = null,
            val winner: Double? = null
        ) {
            data class Team(
                val id: Double? = null,
                val isWinner: Boolean? = null,
                val name: String? = null
            )
        }

        data class Segment(
            val attributes: Attributes? = null,
            val expiryDate: String? = null,
            val metadata: Metadata? = null,
            val stats: Stats? = null,
            val type: String? = null
        ) {
            data class Attributes(
                val categoryKey: String? = null,
                val key: String? = null,
                val platformId: Double? = null,
                val playerId: String? = null,
                val teamId: Double? = null
            )

            data class Metadata(
                val categoryName: String? = null,
                val imageUrl: String? = null,
                val name: String? = null,
                val platformSlug: String? = null,
                val playerName: String? = null
            )

            data class Stats(
                val acesquad: Acesquad? = null,
                val awardScore: AwardScore? = null,
                val bonusScore: BonusScore? = null,
                val deaths: Deaths? = null,
                val flagCaptures: FlagCaptures? = null,
                val generalScore: GeneralScore? = null,
                val headshots: Headshots? = null,
                val headshotsPercentage: HeadshotsPercentage? = null,
                val hits: Hits? = null,
                val kdRatio: KdRatio? = null,
                val kills: Kills? = null,
                val killsPerMinute: KillsPerMinute? = null,
                val lmgsScore: LmgsScore? = null,
                val medicScore: MedicScore? = null,
                val operationsScore: OperationsScore? = null,
                val ordersCompleted: OrdersCompleted? = null,
                val rankScore: RankScore? = null,
                val resupply: Resupply? = null,
                val roundScore: RoundScore? = null,
                val rounds: Rounds? = null,
                val roundsCompleted: RoundsCompleted? = null,
                val rushArtilleryDefenseKills: RushArtilleryDefenseKills? = null,
                val scorePerMinute: ScorePerMinute? = null,
                val shotAccuracy: ShotAccuracy? = null,
                val shots: Shots? = null,
                val shotsAccuracy: ShotsAccuracy? = null,
                val shotsFired: ShotsFired? = null,
                val shotsHit: ShotsHit? = null,
                val soldierDamage: SoldierDamage? = null,
                val squadScore: SquadScore? = null,
                val squadSpawns: SquadSpawns? = null,
                val supportScore: SupportScore? = null,
                val teamScore: TeamScore? = null,
                val time: Time? = null,
                val timePlayed: TimePlayed? = null
            ) {
                data class Acesquad(
                    val category: Any? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    data class Metadata(
                        val `internal`: String? = null
                    )
                }

                data class AwardScore(
                    val category: String? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    data class Metadata(
                        val `internal`: String? = null
                    )
                }

                data class BonusScore(
                    val category: String? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    data class Metadata(
                        val `internal`: String? = null
                    )
                }

                data class Deaths(
                    val category: Any? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    data class Metadata(
                        val `internal`: String? = null
                    )
                }

                data class FlagCaptures(
                    val category: Any? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    data class Metadata(
                        val `internal`: String? = null
                    )
                }

                data class GeneralScore(
                    val category: String? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    data class Metadata(
                        val `internal`: String? = null
                    )
                }

                data class Headshots(
                    val category: Any? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    class Metadata
                }

                data class HeadshotsPercentage(
                    val category: Any? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    class Metadata
                }

                data class Hits(
                    val category: Any? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    data class Metadata(
                        val `internal`: String? = null
                    )
                }

                data class KdRatio(
                    val category: Any? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    class Metadata
                }

                data class Kills(
                    val category: Any? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    data class Metadata(
                        val `internal`: String? = null
                    )
                }

                data class KillsPerMinute(
                    val category: Any? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    class Metadata
                }

                data class LmgsScore(
                    val category: String? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    data class Metadata(
                        val `internal`: String? = null
                    )
                }

                data class MedicScore(
                    val category: String? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    data class Metadata(
                        val `internal`: String? = null
                    )
                }

                data class OperationsScore(
                    val category: String? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    data class Metadata(
                        val `internal`: String? = null
                    )
                }

                data class OrdersCompleted(
                    val category: Any? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    data class Metadata(
                        val `internal`: String? = null
                    )
                }

                data class RankScore(
                    val category: String? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    data class Metadata(
                        val `internal`: String? = null
                    )
                }

                data class Resupply(
                    val category: Any? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    data class Metadata(
                        val `internal`: String? = null
                    )
                }

                data class RoundScore(
                    val category: Any? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    class Metadata
                }

                data class Rounds(
                    val category: Any? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    data class Metadata(
                        val `internal`: String? = null
                    )
                }

                data class RoundsCompleted(
                    val category: Any? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    data class Metadata(
                        val `internal`: String? = null
                    )
                }

                data class RushArtilleryDefenseKills(
                    val category: String? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    data class Metadata(
                        val `internal`: String? = null
                    )
                }

                data class ScorePerMinute(
                    val category: Any? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    class Metadata
                }

                data class ShotAccuracy(
                    val category: Any? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    class Metadata
                }

                data class Shots(
                    val category: Any? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    data class Metadata(
                        val `internal`: String? = null
                    )
                }

                data class ShotsAccuracy(
                    val category: Any? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    class Metadata
                }

                data class ShotsFired(
                    val category: Any? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    class Metadata
                }

                data class ShotsHit(
                    val category: Any? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    class Metadata
                }

                data class SoldierDamage(
                    val category: Any? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    data class Metadata(
                        val `internal`: String? = null
                    )
                }

                data class SquadScore(
                    val category: String? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    data class Metadata(
                        val `internal`: String? = null
                    )
                }

                data class SquadSpawns(
                    val category: Any? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    data class Metadata(
                        val `internal`: String? = null
                    )
                }

                data class SupportScore(
                    val category: String? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    data class Metadata(
                        val `internal`: String? = null
                    )
                }

                data class TeamScore(
                    val category: String? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    data class Metadata(
                        val `internal`: String? = null
                    )
                }

                data class Time(
                    val category: Any? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    data class Metadata(
                        val `internal`: String? = null
                    )
                }

                data class TimePlayed(
                    val category: Any? = null,
                    val description: Any? = null,
                    val displayCategory: Any? = null,
                    val displayName: String? = null,
                    val displayType: String? = null,
                    val displayValue: String? = null,
                    val metadata: Metadata? = null,
                    val percentile: Any? = null,
                    val rank: Any? = null,
                    val value: Double? = null
                ) {
                    class Metadata
                }
            }
        }
    }
}