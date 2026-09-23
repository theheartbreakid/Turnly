package com.crescentapps.turnly.core.util

import java.security.SecureRandom

object RoomCodeGenerator {
    // Unambiguous, uppercase characters avoiding 0/O, 1/I, 5/S
    private const val ALLOWED_CHARS = "2346789ABCDEFGHJKLMNPQRTUVWXYZ"
    private const val CODE_LENGTH = 6
    private val random = SecureRandom()

    /**
     * Generates a short, human-readable 6-character room code.
     * e.g. "X7K9P2"
     */
    fun generateCode(): String {
        val sb = java.lang.StringBuilder(CODE_LENGTH)
        for (i in 0 until CODE_LENGTH) {
            val idx = random.nextInt(ALLOWED_CHARS.length)
            sb.append(ALLOWED_CHARS[idx])
        }
        return sb.toString()
    }

    /**
     * Validates and cleans user-entered room code.
     */
    fun normalizeCode(input: String): String {
        return input.trim().uppercase().replace(" ", "")
    }

    fun isValidCode(input: String): Boolean {
        val normalized = normalizeCode(input)
        if (normalized.length != CODE_LENGTH) return false
        return normalized.all { ALLOWED_CHARS.contains(it) }
    }
}
