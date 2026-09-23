package com.crescentapps.turnly.core.update

import com.crescentapps.turnly.core.update.model.GitHubReleaseAsset
import org.junit.Assert.*
import org.junit.Test

class UpdateSystemTest {

    @Test
    fun semVer_basicComparison() {
        // v1.10.0 must be newer than v1.9.0
        assertTrue(SemVer.isNewer("v1.10.0", "v1.9.0"))
        assertTrue(SemVer.isNewer("1.10.0", "1.9.0"))
        assertFalse(SemVer.isNewer("v1.9.0", "v1.10.0"))

        // Prefix normalization
        assertEquals(0, SemVer.parse("v1.0.0").compareTo(SemVer.parse("1.0.0")))
        assertEquals(0, SemVer.parse("V2.3.4").compareTo(SemVer.parse("v2.3.4")))

        // Major, minor, patch progression
        assertTrue(SemVer.isNewer("2.0.0", "1.99.99"))
        assertTrue(SemVer.isNewer("1.1.0", "1.0.9"))
        assertTrue(SemVer.isNewer("1.0.1", "1.0.0"))
        assertFalse(SemVer.isNewer("1.0.0", "1.0.0"))
        assertFalse(SemVer.isNewer("1.0.0", "1.0.1"))

        // Single and two-segment versions
        assertTrue(SemVer.isNewer("1.1", "1.0"))
        assertTrue(SemVer.isNewer("1.0.1", "1.0"))
        assertFalse(SemVer.isNewer("1.0", "1.0"))
    }

    @Test
    fun releaseAssetSelector_filtersOutInvalidFiles() {
        val assets = listOf(
            GitHubReleaseAsset(name = "Turnly-v1.2.0-debug.apk", browserDownloadUrl = "http://test/debug.apk"),
            GitHubReleaseAsset(name = "mapping.txt", browserDownloadUrl = "http://test/mapping.txt"),
            GitHubReleaseAsset(name = "source-code.zip", browserDownloadUrl = "http://test/source.zip"),
            GitHubReleaseAsset(name = "checksums.sha256", browserDownloadUrl = "http://test/sha.txt"),
            GitHubReleaseAsset(name = "Turnly-v1.2.0-universal.apk", browserDownloadUrl = "http://test/universal.apk")
        )

        val selected = ReleaseAssetSelector.selectBestApk(assets)
        assertNotNull(selected)
        assertEquals("Turnly-v1.2.0-universal.apk", selected?.name)
    }

    @Test
    fun releaseAssetSelector_selectsSingleValidApk() {
        val assets = listOf(
            GitHubReleaseAsset(name = "Turnly-v1.2.0.apk", browserDownloadUrl = "http://test/app.apk"),
            GitHubReleaseAsset(name = "notes.txt", browserDownloadUrl = "http://test/notes.txt")
        )

        val selected = ReleaseAssetSelector.selectBestApk(assets)
        assertNotNull(selected)
        assertEquals("Turnly-v1.2.0.apk", selected?.name)
    }

    @Test
    fun releaseAssetSelector_noValidApk_returnsNull() {
        val assets = listOf(
            GitHubReleaseAsset(name = "Turnly-debug.apk", browserDownloadUrl = "http://test/debug.apk"),
            GitHubReleaseAsset(name = "source.tar.gz", browserDownloadUrl = "http://test/source.tar.gz")
        )

        val selected = ReleaseAssetSelector.selectBestApk(assets)
        assertNull(selected)
    }
}
