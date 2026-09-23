package com.crescentapps.turnly.core.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.crescentapps.turnly.core.update.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * UpdateManager handles GitHub release querying, APK download, pre-installation verification,
 * and system package installer launching for Turnly.
 *
 * Source of truth: https://github.com/theheartbreakid/Turnly
 */
class UpdateManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    companion object {
        const val REPO_OWNER = "theheartbreakid"
        const val REPO_NAME = "Turnly"
        const val GITHUB_LATEST_RELEASE_URL = "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/releases/latest"
        const val FILE_PROVIDER_AUTHORITY = "com.crescentapps.turnly.fileprovider"

        @Volatile
        private var INSTANCE: UpdateManager? = null

        fun getInstance(context: Context): UpdateManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UpdateManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private var downloadJob: Job? = null

    val currentVersionName: String
        get() = try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }

    val currentVersionCode: Long
        get() = try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
        } catch (e: Exception) {
            1L
        }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Checks for updates against GitHub Releases API.
     * @param isManual True if initiated by the user tapping "Check for Updates"
     */
    suspend fun checkForUpdates(isManual: Boolean = false): Result<UpdateInfo?> = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable()) {
            val error = UpdateState.Error("No internet connection available.", UpdateErrorType.NETWORK, canRetry = true)
            if (isManual) _updateState.value = error
            return@withContext Result.failure(Exception(error.message))
        }

        if (isManual) {
            _updateState.value = UpdateState.Checking
        }

        try {
            val url = URL(GITHUB_LATEST_RELEASE_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "Turnly-Android-App")
                connectTimeout = 15000
                readTimeout = 15000
                instanceFollowRedirects = true
            }

            val responseCode = connection.responseCode
            if (responseCode == 403 || responseCode == 429) {
                val error = UpdateState.Error(
                    "GitHub API rate limit exceeded. Please try again later.",
                    UpdateErrorType.RATE_LIMIT,
                    canRetry = false
                )
                if (isManual) _updateState.value = error
                return@withContext Result.failure(Exception(error.message))
            }

            if (responseCode !in 200..299) {
                val error = UpdateState.Error(
                    "GitHub server error ($responseCode).",
                    UpdateErrorType.SERVER,
                    canRetry = true
                )
                if (isManual) _updateState.value = error
                return@withContext Result.failure(Exception(error.message))
            }

            val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
            val release = json.decodeFromString<GitHubRelease>(responseBody)

            // Validate that release is stable and published
            if (release.draft || release.prerelease) {
                val state = UpdateState.UpToDate(currentVersionName, System.currentTimeMillis())
                if (isManual) _updateState.value = state
                return@withContext Result.success(null)
            }

            val bestAsset = ReleaseAssetSelector.selectBestApk(release.assets)
            if (bestAsset == null) {
                val error = UpdateState.Error(
                    "Release ${release.tagName} contains no compatible APK for this device.",
                    UpdateErrorType.NO_COMPATIBLE_APK,
                    canRetry = false
                )
                if (isManual) _updateState.value = error
                return@withContext Result.failure(Exception(error.message))
            }

            val isNewer = SemVer.isNewer(release.tagName, currentVersionName)
            if (!isNewer) {
                val state = UpdateState.UpToDate(currentVersionName, System.currentTimeMillis())
                if (isManual) _updateState.value = state
                return@withContext Result.success(null)
            }

            val updateInfo = UpdateInfo(
                versionName = release.tagName.trim().removePrefix("v").removePrefix("V"),
                releaseTitle = release.name ?: release.tagName,
                releaseNotes = release.body ?: "No release notes provided.",
                publishedAt = release.publishedAt ?: "",
                downloadUrl = bestAsset.browserDownloadUrl,
                assetName = bestAsset.name,
                assetSize = bestAsset.size,
                htmlUrl = release.htmlUrl ?: "https://github.com/$REPO_OWNER/$REPO_NAME/releases/latest"
            )

            // If we already downloaded and validated this exact release APK, transition directly to ReadyToInstall
            val existingApk = getDownloadedApkFile(updateInfo.versionName)
            if (existingApk.exists() && existingApk.length() > 0 && verifyApk(existingApk, updateInfo.versionName) == null) {
                if (canRequestPackageInstalls()) {
                    _updateState.value = UpdateState.ReadyToInstall(updateInfo, existingApk)
                } else {
                    _updateState.value = UpdateState.WaitingForInstallPermission(updateInfo, existingApk)
                }
            } else {
                _updateState.value = UpdateState.UpdateAvailable(updateInfo)
            }

            Result.success(updateInfo)
        } catch (e: Exception) {
            val error = UpdateState.Error(
                e.message ?: "Failed to check for updates.",
                UpdateErrorType.NETWORK,
                canRetry = true
            )
            if (isManual) _updateState.value = error
            Result.failure(e)
        }
    }

    /**
     * Downloads the APK in the background with progress reporting and cancellation support.
     */
    fun startDownload(updateInfo: UpdateInfo) {
        if (_updateState.value is UpdateState.Downloading) return

        downloadJob?.cancel()
        downloadJob = scope.launch(Dispatchers.IO) {
            val apkFile = getDownloadedApkFile(updateInfo.versionName)
            cleanOldApksExcept(apkFile)

            _updateState.value = UpdateState.Downloading(
                updateInfo = updateInfo,
                downloadedBytes = 0L,
                totalBytes = updateInfo.assetSize,
                progress = 0f
            )

            var input: InputStream? = null
            var output: FileOutputStream? = null
            var connection: HttpURLConnection? = null

            try {
                var currentUrl = updateInfo.downloadUrl
                var redirects = 0
                while (redirects < 5) {
                    connection = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        setRequestProperty("User-Agent", "Turnly-Android-App")
                        connectTimeout = 20000
                        readTimeout = 20000
                        instanceFollowRedirects = false
                    }

                    val code = connection.responseCode
                    if (code in listOf(301, 302, 303, 307, 308)) {
                        currentUrl = connection.getHeaderField("Location") ?: break
                        connection.disconnect()
                        redirects++
                    } else {
                        break
                    }
                }

                if (connection == null || connection.responseCode !in 200..299) {
                    _updateState.value = UpdateState.Error(
                        "Download failed with HTTP ${connection?.responseCode ?: -1}.",
                        UpdateErrorType.DOWNLOAD_FAILED,
                        canRetry = true
                    )
                    return@launch
                }

                val totalLength = connection.contentLengthLong.takeIf { it > 0 } ?: updateInfo.assetSize
                input = connection.inputStream
                output = FileOutputStream(apkFile)

                val buffer = ByteArray(8 * 1024)
                var bytesCopied = 0L
                var read: Int

                while (input.read(buffer).also { read = it } >= 0) {
                    ensureActive()
                    output.write(buffer, 0, read)
                    bytesCopied += read

                    val progress = if (totalLength > 0) {
                        (bytesCopied.toFloat() / totalLength.toFloat()).coerceIn(0f, 1f)
                    } else {
                        0f
                    }

                    _updateState.value = UpdateState.Downloading(
                        updateInfo = updateInfo,
                        downloadedBytes = bytesCopied,
                        totalBytes = totalLength,
                        progress = progress
                    )
                }

                output.flush()

                // Validate the downloaded APK
                val verificationError = verifyApk(apkFile, updateInfo.versionName)
                if (verificationError != null) {
                    apkFile.delete()
                    _updateState.value = verificationError
                    return@launch
                }

                // Check install permission
                if (canRequestPackageInstalls()) {
                    _updateState.value = UpdateState.ReadyToInstall(updateInfo, apkFile)
                } else {
                    _updateState.value = UpdateState.WaitingForInstallPermission(updateInfo, apkFile)
                }
            } catch (e: CancellationException) {
                apkFile.delete()
                _updateState.value = UpdateState.UpdateAvailable(updateInfo)
            } catch (e: Exception) {
                apkFile.delete()
                _updateState.value = UpdateState.Error(
                    e.message ?: "Failed to download update APK.",
                    UpdateErrorType.DOWNLOAD_FAILED,
                    canRetry = true
                )
            } finally {
                try { input?.close() } catch (_: Exception) {}
                try { output?.close() } catch (_: Exception) {}
                connection?.disconnect()
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
        val current = _updateState.value
        if (current is UpdateState.Downloading) {
            _updateState.value = UpdateState.UpdateAvailable(current.updateInfo)
        }
    }

    /**
     * Authoritative pre-installation APK verification.
     * Verifies:
     * - File exists and is non-empty.
     * - Valid Android package archive.
     * - Package name exactly matches Turnly ("com.crescentapps.turnly").
     * - Version code / version name is appropriate.
     * - Signature compatibility with installed package.
     */
    fun verifyApk(apkFile: File, expectedVersionName: String): UpdateState.Error? {
        if (!apkFile.exists() || apkFile.length() == 0L) {
            return UpdateState.Error("Downloaded update file is missing or corrupted.", UpdateErrorType.VERIFICATION_FAILED)
        }

        val packageManager = context.packageManager
        val archiveInfo = packageManager.getPackageArchiveInfo(
            apkFile.absolutePath,
            PackageManager.GET_ACTIVITIES or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) PackageManager.GET_SIGNING_CERTIFICATES else @Suppress("DEPRECATION") PackageManager.GET_SIGNATURES)
        ) ?: return UpdateState.Error("Downloaded file is not a valid Android APK package.", UpdateErrorType.VERIFICATION_FAILED)

        // 1. Verify package name
        if (archiveInfo.packageName != context.packageName) {
            return UpdateState.Error(
                "Downloaded APK package (${archiveInfo.packageName}) does not match Turnly (${context.packageName}).",
                UpdateErrorType.VERIFICATION_FAILED
            )
        }

        // 2. Verify version code
        val apkVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            archiveInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            archiveInfo.versionCode.toLong()
        }

        // Verify version is newer or equal to expected release
        val apkVersionName = archiveInfo.versionName ?: ""
        if (!SemVer.isNewer(apkVersionName, currentVersionName) && apkVersionCode <= currentVersionCode) {
            return UpdateState.Error(
                "Downloaded APK (v$apkVersionName) is not newer than currently installed Turnly (v$currentVersionName).",
                UpdateErrorType.VERIFICATION_FAILED
            )
        }

        // 3. Verify signature compatibility where possible
        val isSignatureCompatible = checkSignatureCompatibility(archiveInfo)
        if (!isSignatureCompatible) {
            return UpdateState.Error(
                "Update package is signed with an incompatible certificate. It cannot replace the installed version.",
                UpdateErrorType.INCOMPATIBLE_SIGNATURE
            )
        }

        return null
    }

    private fun checkSignatureCompatibility(archiveInfo: android.content.pm.PackageInfo): Boolean {
        return try {
            val pm = context.packageManager
            val installedInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val installedHistory = installedInfo.signingInfo?.signingCertificateHistory ?: return true
                val apkHistory = archiveInfo.signingInfo?.signingCertificateHistory ?: return true
                if (installedHistory.isNotEmpty() && apkHistory.isNotEmpty()) {
                    installedHistory[0].toByteArray().contentEquals(apkHistory[0].toByteArray())
                } else {
                    true
                }
            } else {
                @Suppress("DEPRECATION")
                val installedSigs = installedInfo.signatures ?: return true
                @Suppress("DEPRECATION")
                val apkSigs = archiveInfo.signatures ?: return true
                if (installedSigs.isNotEmpty() && apkSigs.isNotEmpty()) {
                    installedSigs[0].toByteArray().contentEquals(apkSigs[0].toByteArray())
                } else {
                    true
                }
            }
        } catch (e: Exception) {
            // Let the system package installer perform authoritative verification if check fails
            true
        }
    }

    /**
     * Checks if Turnly is permitted to install apps from this source.
     */
    fun canRequestPackageInstalls(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    /**
     * Creates an intent navigating directly to Turnly's unknown app sources settings.
     */
    fun createManageUnknownAppSourcesIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
    }

    /**
     * Re-checks permission upon returning from system settings.
     */
    fun onReturnFromSettings() {
        val current = _updateState.value
        if (current is UpdateState.WaitingForInstallPermission) {
            if (canRequestPackageInstalls()) {
                _updateState.value = UpdateState.ReadyToInstall(current.updateInfo, current.apkFile)
            }
        }
    }

    /**
     * Launches the Android package installer for the downloaded APK using FileProvider.
     */
    fun installApk(apkFile: File, updateInfo: UpdateInfo): Boolean {
        if (!canRequestPackageInstalls()) {
            _updateState.value = UpdateState.WaitingForInstallPermission(updateInfo, apkFile)
            return false
        }

        try {
            val contentUri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, apkFile)
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(installIntent)
            _updateState.value = UpdateState.Installing(updateInfo)
            return true
        } catch (e: Exception) {
            _updateState.value = UpdateState.Error(
                "Failed to launch package installer: ${e.localizedMessage}",
                UpdateErrorType.INSTALLATION_FAILED,
                canRetry = true
            )
            return false
        }
    }

    fun dismissState() {
        _updateState.value = UpdateState.Idle
    }

    private fun getDownloadedApkFile(versionName: String): File {
        val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
        return File(updatesDir, "Turnly-$versionName.apk")
    }

    private fun cleanOldApksExcept(activeFile: File?) {
        try {
            val updatesDir = File(context.cacheDir, "updates")
            if (updatesDir.exists() && updatesDir.isDirectory) {
                updatesDir.listFiles()?.forEach { file ->
                    if (file.isFile && file.absolutePath != activeFile?.absolutePath) {
                        file.delete()
                    }
                }
            }
        } catch (_: Exception) {}
    }
}
