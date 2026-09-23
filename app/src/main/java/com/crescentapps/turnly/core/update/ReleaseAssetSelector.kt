package com.crescentapps.turnly.core.update

import android.os.Build
import com.crescentapps.turnly.core.update.model.GitHubReleaseAsset

/**
 * Intelligent selector for release APK assets.
 * Inspects all release assets and prefers the APK intended for the current Turnly Android distribution.
 *
 * Rules:
 * 1. Must be an .apk file.
 * 2. Reject debug, test, mapping, checksum, and source archives.
 * 3. Match device ABI (arm64-v8a, armeabi-v7a, x86_64) if ABI-specific APKs exist.
 * 4. Fallback to universal or general release APK (e.g. Turnly-release.apk, Turnly-universal.apk, app-release.apk).
 */
object ReleaseAssetSelector {

    private val EXCLUDED_SUFFIXES = listOf(
        "-debug.apk",
        "_debug.apk",
        ".zip",
        ".tar.gz",
        ".txt",
        ".sha256",
        ".md5",
        "mapping.txt"
    )

    fun selectBestApk(assets: List<GitHubReleaseAsset>): GitHubReleaseAsset? {
        val apkAssets = assets.filter { asset ->
            val lower = asset.name.lowercase()
            lower.endsWith(".apk") && EXCLUDED_SUFFIXES.none { lower.endsWith(it) }
        }

        if (apkAssets.isEmpty()) return null
        if (apkAssets.size == 1) return apkAssets.first()

        val supportedAbis = Build.SUPPORTED_ABIS.map { it.lowercase() }

        // Look for exact ABI matches in priority order of device support
        for (abi in supportedAbis) {
            val abiMatch = apkAssets.firstOrNull { it.name.lowercase().contains(abi) }
            if (abiMatch != null) return abiMatch
        }

        // Look for "universal"
        val universal = apkAssets.firstOrNull { it.name.lowercase().contains("universal") }
        if (universal != null) return universal

        // Look for release APK without ABI specified
        val releaseApk = apkAssets.firstOrNull {
            val lower = it.name.lowercase()
            lower.contains("release") && !lower.contains("debug")
        }
        if (releaseApk != null) return releaseApk

        // If none specifically matched ABI or universal but there are APKs, pick the first valid non-debug APK
        return apkAssets.firstOrNull()
    }
}
