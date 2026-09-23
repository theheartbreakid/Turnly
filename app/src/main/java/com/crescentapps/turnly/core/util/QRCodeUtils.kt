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
            val content = rawContent.trim()
            val base64Data = when {
                content.startsWith("turnly://room/") -> content.removePrefix("turnly://room/")
                content.startsWith("http") && content.contains("/room/") -> content.substringAfter("/room/")
                else -> content
            }
            val jsonBytes = java.util.Base64.getUrlDecoder().decode(base64Data)
            val jsonStr = String(jsonBytes, Charsets.UTF_8)
            json.decodeFromString<RoomInvitePayload>(jsonStr)
        }.getOrNull()
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
