package com.crescentapps.turnly.core.update.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

/**
 * GitHub Releases API data models.
 * API: https://api.github.com/repos/theheartbreakid/Turnly/releases/latest
 */
@Serializable
data class GitHubRelease(
    @SerialName("tag_name")
    val tagName: String,
    val name: String? = null,
    val body: String? = null,
    @SerialName("published_at")
    val publishedAt: String? = null,
    @SerialName("html_url")
    val htmlUrl: String? = null,
    val draft: Boolean = false,
    val prerelease: Boolean = false,
    val assets: List<GitHubReleaseAsset> = emptyList()
)

@Serializable
data class GitHubReleaseAsset(
    val name: String,
    val size: Long = 0L,
    @SerialName("browser_download_url")
    val browserDownloadUrl: String,
    @SerialName("content_type")
    val contentType: String? = null
)

/**
 * Clean domain model for an update detected from GitHub.
 */
data class UpdateInfo(
    val versionName: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val publishedAt: String,
    val downloadUrl: String,
    val assetName: String,
    val assetSize: Long,
    val htmlUrl: String
)

/**
 * Explicit update state model representing every step in the update lifecycle.
 * Avoids conflicting boolean flags.
 */
sealed class UpdateState {
    data object Idle : UpdateState()
    data object Checking : UpdateState()
    data class UpToDate(val currentVersion: String, val checkedAt: Long) : UpdateState()
    data class UpdateAvailable(val updateInfo: UpdateInfo) : UpdateState()
    data class Downloading(
        val updateInfo: UpdateInfo,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val progress: Float
    ) : UpdateState()
    data class DownloadCompleted(val updateInfo: UpdateInfo, val apkFile: File) : UpdateState()
    data class WaitingForInstallPermission(val updateInfo: UpdateInfo, val apkFile: File) : UpdateState()
    data class ReadyToInstall(val updateInfo: UpdateInfo, val apkFile: File) : UpdateState()
    data class Installing(val updateInfo: UpdateInfo) : UpdateState()
    data class Error(
        val message: String,
        val errorType: UpdateErrorType,
        val canRetry: Boolean = true
    ) : UpdateState()
}

enum class UpdateErrorType {
    NETWORK,
    RATE_LIMIT,
    SERVER,
    NO_COMPATIBLE_APK,
    DOWNLOAD_FAILED,
    VERIFICATION_FAILED,
    INCOMPATIBLE_SIGNATURE,
    INSTALLATION_FAILED,
    UNKNOWN
}
