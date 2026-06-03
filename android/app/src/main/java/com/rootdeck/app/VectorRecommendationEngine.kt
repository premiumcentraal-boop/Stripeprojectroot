package com.rootdeck.app

data class VectorRecommendation(
    val framework: String,
    val installType: String,
    val source: String,
    val note: String,
    val asset: VectorAsset?,
)

object VectorRecommendationEngine {
    fun recommend(env: RootEnvironment, release: VectorRelease?): VectorRecommendation {
        val asset = release?.assets
            ?.filter { it.name.endsWith(".zip", ignoreCase = true) }
            ?.filterNot { it.name.contains("riru", ignoreCase = true) }
            ?.sortedWith(
                compareByDescending<VectorAsset> { it.name.contains("zygisk", ignoreCase = true) }
                    .thenBy { it.name.contains("debug", ignoreCase = true) }
            )
            ?.firstOrNull()
        val note = if (env.sdkInt >= 35) {
            "Android ${env.sdkInt}: strongly recommend the latest Vector Zygisk release."
        } else {
            "Recommend Vector first. Legacy LSPosed is only for older special setups."
        }
        return VectorRecommendation(
            framework = "Vector, formerly LSPosed",
            installType = "Zygisk Magisk module ZIP",
            source = "Official GitHub releases from JingMatrix/Vector",
            note = note,
            asset = asset,
        )
    }
}
