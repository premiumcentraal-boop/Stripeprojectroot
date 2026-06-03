package com.rootdeck.app

data class VectorInstallAsset(
    val versionTag: String,
    val releaseUrl: String,
    val zipFileName: String?,
    val downloadUrl: String?,
)

object VectorReleaseFinder {
    suspend fun findLatestRecommended(): Result<VectorInstallAsset> = VectorReleaseRepository.fetchLatest().map { release ->
        val zipAssets = release.assets.filter { it.name.endsWith(".zip", ignoreCase = true) }
        val stableNonRiru = zipAssets
            .filterNot { it.name.contains("riru", ignoreCase = true) }
            .filterNot { it.name.contains("debug", ignoreCase = true) }
        val debugFallback = zipAssets
            .filterNot { it.name.contains("riru", ignoreCase = true) }
        val pool = stableNonRiru.ifEmpty { debugFallback }.ifEmpty { zipAssets }
        val best = pool.sortedWith(
            compareByDescending<VectorAsset> { it.name.contains("zygisk", ignoreCase = true) }
                .thenBy { it.name.contains("debug", ignoreCase = true) }
        ).firstOrNull()

        VectorInstallAsset(
            versionTag = release.tagName.ifBlank { release.name },
            releaseUrl = release.htmlUrl,
            zipFileName = best?.name,
            downloadUrl = best?.browserDownloadUrl,
        )
    }
}
