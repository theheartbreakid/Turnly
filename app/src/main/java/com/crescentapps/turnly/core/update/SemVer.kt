package com.crescentapps.turnly.core.update

/**
 * Robust semantic version parser and comparator.
 * Normalizes leading 'v' or 'V' prefixes and handles versions like:
 * "1.0", "v1.2.3", "1.10.0", "v2.0.0-rc1".
 *
 * Ensures non-lexicographical comparison so that "v1.10.0" > "v1.9.0".
 */
data class SemVer(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val preRelease: String = "",
    val raw: String
) : Comparable<SemVer> {

    override fun compareTo(other: SemVer): Int {
        if (this.major != other.major) {
            return this.major.compareTo(other.major)
        }
        if (this.minor != other.minor) {
            return this.minor.compareTo(other.minor)
        }
        if (this.patch != other.patch) {
            return this.patch.compareTo(other.patch)
        }
        // A release with a pre-release version has lower precedence than a normal release.
        // e.g. 1.0.0-alpha < 1.0.0
        return when {
            this.preRelease.isEmpty() && other.preRelease.isNotEmpty() -> 1
            this.preRelease.isNotEmpty() && other.preRelease.isEmpty() -> -1
            else -> this.preRelease.compareTo(other.preRelease)
        }
    }

    companion object {
        fun parse(versionString: String): SemVer {
            val trimmed = versionString.trim().removePrefix("v").removePrefix("V")
            val parts = trimmed.split('-', limit = 2)
            val mainVersion = parts[0]
            val preRelease = if (parts.size > 1) parts[1] else ""

            val segments = mainVersion.split('.')
            val major = segments.getOrNull(0)?.toIntOrNull() ?: 0
            val minor = segments.getOrNull(1)?.toIntOrNull() ?: 0
            val patch = segments.getOrNull(2)?.toIntOrNull() ?: 0

            return SemVer(
                major = major,
                minor = minor,
                patch = patch,
                preRelease = preRelease,
                raw = versionString
            )
        }

        fun isNewer(latestVersion: String, currentVersion: String): Boolean {
            val latest = parse(latestVersion)
            val current = parse(currentVersion)
            return latest > current
        }
    }
}
