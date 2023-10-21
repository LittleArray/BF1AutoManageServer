package data

data class GatewayWeapons(
    val id: String,
    val jsonrpc: String,
    val result: List<Result>
) {
    data class Result(
        val categoryId: String,
        val name: String,
        val weapons: List<Weapon>
    ) {
        data class Weapon(
            val accessories: List<Any>,
            val category: String,
            val description: String,
            val expansion: String,
            val extendedStats: Any,
            val guid: String,
            val hires: Hires,
            val imageUrl: String,
            val images: Images,
            val info: Info,
            val kitShortcutLicenses: List<Any>,
            val name: String,
            val premium: Boolean,
            val price: Int,
            val progression: Progression,
            val purchaseInfo: PurchaseInfo,
            val rank: Any,
            val star: Star,
            val stats: Stats,
            val svgImage: Any,
            val unlockRequirements: List<UnlockRequirement>
        ) {
            data class Hires(
                val Png1024xANY: String,
                val Png256xANY: String,
                val Png300xANY: String,
                val Small: String
            )

            data class Images(
                val Png256xANY: String,
                val Small: String
            )

            data class Info(
                val ammo: String,
                val ammoType: String,
                val canBreakWood: Boolean,
                val canCutBarbedWire: Boolean,
                val canDamageLightVehicle: Boolean,
                val damageDropPoints: List<DamageDropPoint>,
                val fireModeAuto: Boolean,
                val fireModeBurst: Boolean,
                val fireModeSingle: Boolean,
                val numberOfMagazines: Int,
                val range: String,
                val rateOfFire: String,
                val statAccuracy: Double,
                val statAttackSpeed: Int,
                val statControl: Double,
                val statDamage: Double,
                val statFireRate: Int,
                val statHandling: Double,
                val statMobility: Double,
                val statRange: Double,
                val statReload: Int
            ) {
                data class DamageDropPoint(
                    val x: Double,
                    val y: Double
                )
            }

            data class Progression(
                val unlocked: Boolean,
                val valueAcquired: Int,
                val valueNeeded: Int
            )

            data class PurchaseInfo(
                val purchasePrice: Int
            )

            data class Star(
                val imageUrl: String,
                val images: Any,
                val progression: Progression,
                val timesAquired: Int
            ) {
                data class Progression(
                    val unlocked: Boolean,
                    val valueAcquired: Int,
                    val valueNeeded: Int
                )
            }

            data class Stats(
                val values: Values?=null
            ) {
                data class Values(
                    val accuracy: Double?=null,
                    val destroyed: Int?=null,
                    val headshots: Int?=null,
                    val hits: Int?=null,
                    val kills: Int?=null,
                    val seconds: Double?=null,
                    val shots: Int?=null
                )
            }

            data class UnlockRequirement(
                val code: String,
                val criterias: List<Criteria>,
                val progression: Progression,
                val unlockType: String
            ) {
                data class Criteria(
                    val awardName: String,
                    val code: String,
                    val criteriaType: String,
                    val name: String,
                    val progression: Progression
                ) {
                    data class Progression(
                        val unlocked: Boolean,
                        val valueAcquired: Double,
                        val valueNeeded: Int
                    )
                }

                data class Progression(
                    val unlocked: Boolean,
                    val valueAcquired: Int,
                    val valueNeeded: Int
                )
            }
        }
    }
}