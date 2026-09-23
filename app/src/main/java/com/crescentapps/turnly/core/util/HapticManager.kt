package com.crescentapps.turnly.core.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * SHARED HAPTIC MANAGER
 * Adapted from DhikrCounter visual system.
 * Centralizes all haptic feedback logic with intensity scaling.
 */
class HapticManager(private val context: Context) {

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun vibrate(baseDuration: Long = 50L, intensityOverride: Float? = null, enabled: Boolean = true) {
        if (!enabled) return
        val intensity = (intensityOverride ?: 1.0f).coerceIn(0f, 1f)

        if (intensity <= 0f || vibrator == null) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amplitude = (255 * intensity).toInt().coerceIn(1, 255)
                try {
                    vibrator?.vibrate(VibrationEffect.createOneShot(baseDuration, amplitude))
                } catch (e: Exception) {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate((baseDuration * intensity).toLong().coerceAtLeast(1L))
                }
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate((baseDuration * intensity).toLong().coerceAtLeast(1L))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun vibrateSubtle(intensity: Float = 1.0f, enabled: Boolean = true) {
        vibrate(30L, intensity * 0.5f, enabled)
    }

    fun vibrateStrong(intensity: Float = 1.0f, enabled: Boolean = true) {
        vibrate(80L, intensity * 1.5f, enabled)
    }
}
