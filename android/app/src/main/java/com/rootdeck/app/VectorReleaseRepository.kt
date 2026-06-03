package com.rootdeck.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val VECTOR_LATEST_RELEASE_API = "https://api.github.com/repos/JingMatrix/Vector/releases/latest"

data class VectorAsset(
    val name: String,
    val browserDownloadUrl: String,
    val size: Long,
    val contentType: String?,
)

data class VectorRelease(
    val tagName: String,
    val name: String,
    val htmlUrl: String,
    val publishedAt: String?,
    val body: String?,
    val assets: List<VectorAsset>,
)

object VectorReleaseRepository {
    suspend fun fetchLatest(): Result<VectorRelease> = withContext(Dispatchers.IO) {
        runCatching {
            val connection = URL(VECTOR_LATEST_RELEASE_API).openConnection() as HttpURLConnection
            connection.connectTimeout = 20_000
            connection.readTimeout = 20_000
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("User-Agent", "RootDeck")
            val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            val assetsJson = json.getJSONArray("assets")
            val assets = buildList {
                for (i in 0 until assetsJson.length()) {
                    val asset = assetsJson.getJSONObject(i)
                    add(
                        VectorAsset(
                            name = asset.optString("name"),
                            browserDownloadUrl = asset.optString("browser_download_url"),
                            size = asset.optLong("size"),
                            contentType = asset.optString("content_type").ifBlank { null },
                        ),
                    )
                }
            }
            VectorRelease(
                tagName = json.optString("tag_name"),
                name = json.optString("name", json.optString("tag_name")),
                htmlUrl = json.optString("html_url"),
                publishedAt = json.optString("published_at").ifBlank { null },
                body = json.optString("body").ifBlank { null },
                assets = assets,
            )
        }
    }
}
