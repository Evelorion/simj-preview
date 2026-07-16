package com.sansim.app.update

import com.sansim.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val versionName: String,
    val changelog: String,
    val downloadUrl: String,
    val htmlUrl: String
)

object UpdateChecker {
    private val repoOwner = BuildConfig.SIMJ_UPDATE_REPO_OWNER.trim()
    private val repoName = BuildConfig.SIMJ_UPDATE_REPO_NAME.trim()

    suspend fun check(currentVersion: String): UpdateInfo? = withContext(Dispatchers.IO) {
        if (repoOwner.isBlank() || repoName.isBlank()) return@withContext null
        runCatching {
            val url = URL("https://api.github.com/repos/$repoOwner/$repoName/releases/latest")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json")
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            val text = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val json = JSONObject(text)
            val tagName = json.optString("tag_name", "")
            val remoteVersion = tagName.removePrefix("v")
            val localVersion = currentVersion.removePrefix("v")

            if (isNewer(remoteVersion, localVersion)) {
                val body = json.optString("body", "")
                val htmlUrl = json.optString("html_url", "")
                val assets = json.optJSONArray("assets")
                var downloadUrl = htmlUrl
                if (assets != null && assets.length() > 0) {
                    downloadUrl = assets.getJSONObject(0).optString("browser_download_url", htmlUrl)
                }
                UpdateInfo(
                    versionName = remoteVersion,
                    changelog = body,
                    downloadUrl = downloadUrl,
                    htmlUrl = htmlUrl
                )
            } else null
        }.getOrNull()
    }

    private data class VersionParts(val nums: List<Int>, val pre: Boolean)

    private fun parseVersion(v: String): VersionParts {
        val clean = v.removePrefix("v").trim()
        val pre = clean.contains("-pre", ignoreCase = true)
        val nums = Regex("\\d+").findAll(clean.substringBefore('-')).map { it.value.toIntOrNull() ?: 0 }.toList()
        return VersionParts(nums, pre)
    }

    private fun isNewer(remote: String, local: String): Boolean {
        val r = parseVersion(remote)
        val l = parseVersion(local)
        val len = maxOf(r.nums.size, l.nums.size)
        for (i in 0 until len) {
            val rv = r.nums.getOrElse(i) { 0 }
            val lv = l.nums.getOrElse(i) { 0 }
            if (rv > lv) return true
            if (rv < lv) return false
        }
        // Same numeric version: stable is newer than pre-release, same channel is not newer.
        return l.pre && !r.pre
    }
}
