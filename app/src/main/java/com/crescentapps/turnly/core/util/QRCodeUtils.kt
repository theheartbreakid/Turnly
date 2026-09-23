package com.crescentapps.turnly.core.util

import android.graphics.Bitmap
import android.graphics.Color
import com.crescentapps.turnly.core.model.RoomInvitePayload
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object QRCodeUtils {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Serializes RoomInvitePayload to deep link URL turnly://room/...
     */
    fun createInviteDeepLink(payload: RoomInvitePayload): String {
        val payloadJson = json.encodeToString(payload)
        val encodedBase64 = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(
            payloadJson.toByteArray(Charsets.UTF_8)
        )
        return "turnly://room/$encodedBase64"
    }

    /**
     * Decodes QR code content or deep link URI into RoomInvitePayload.
     */
    fun parseInvitePayload(rawContent: String): RoomInvitePayload? {
        return runCatching {
            var content = rawContent.trim()
            // In case of URI encoded string
            if (content.contains("%")) {
                content = runCatching { java.net.URLDecoder.decode(content, "UTF-8") }.getOrDefault(content)
            }
            val base64Data = when {
                content.startsWith("turnly://room/") -> content.removePrefix("turnly://room/").substringBefore("?").substringBefore("/")
                content.startsWith("http") && content.contains("/room/") -> content.substringAfter("/room/").substringBefore("?").substringBefore("/")
                else -> content.substringBefore("?").substringBefore("/")
            }
            val jsonBytes = try {
                java.util.Base64.getUrlDecoder().decode(base64Data)
            } catch (e: Exception) {
                java.util.Base64.getDecoder().decode(base64Data)
            }
            val jsonStr = String(jsonBytes, Charsets.UTF_8)
            json.decodeFromString<RoomInvitePayload>(jsonStr)
        }.getOrNull()
    }

    /**
     * Extracts the exact room code from any valid Turnly QR code payload,
     * deep link URI (turnly://room/...), HTTP fallback link, or direct 6-character room code.
     */
    fun extractRoomCode(rawContent: String): String? {
        val trimmed = rawContent.trim()
        if (trimmed.isEmpty()) return null

        // 1. Direct valid room code (e.g. "X7K9P2")
        val directNormalized = RoomCodeGenerator.normalizeCode(trimmed)
        if (RoomCodeGenerator.isValidCode(directNormalized)) {
            return directNormalized
        }

        // 2. Query parameter (?code=XYZ123)
        if (trimmed.contains("code=")) {
            val extractedParam = trimmed.substringAfter("code=").substringBefore("&").substringBefore("#")
            val normalizedParam = RoomCodeGenerator.normalizeCode(extractedParam)
            if (RoomCodeGenerator.isValidCode(normalizedParam)) {
                return normalizedParam
            }
        }

        // 3. Encoded RoomInvitePayload in deep link or raw Base64
        val payload = parseInvitePayload(trimmed)
        if (payload != null) {
            val code = RoomCodeGenerator.normalizeCode(payload.roomCode)
            if (RoomCodeGenerator.isValidCode(code)) {
                return code
            }
            // If custom valid code format
            if (payload.roomCode.isNotBlank()) {
                return payload.roomCode.trim()
            }
        }

        // 4. Path parameter if directly formatted as turnly://room/X7K9P2
        if (trimmed.startsWith("turnly://room/")) {
            val pathPart = trimmed.removePrefix("turnly://room/").substringBefore("?").substringBefore("/").trim()
            val normalizedPath = RoomCodeGenerator.normalizeCode(pathPart)
            if (RoomCodeGenerator.isValidCode(normalizedPath)) {
                return normalizedPath
            }
        }

        return null
    }

    /**
     * Generates a high-contrast Android Bitmap representing the QR code.
     */
    fun generateQRCodeBitmap(
        content: String,
        width: Int = 512,
        height: Int = 512
    ): Bitmap {
        val hints = hashMapOf<EncodeHintType, Any>(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.Q,
            EncodeHintType.MARGIN to 1
        )
        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}
