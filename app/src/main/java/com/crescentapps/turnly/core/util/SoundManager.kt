package com.crescentapps.turnly.core.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import com.crescentapps.turnly.R

/**
 * SOUND MANAGER FOR TURNLY
 * Adapted from DhikrCounter sound architecture.
 * Plays sound feedback for completion, steps, confirmations, and resets
 * respecting system ringer mode and user settings.
 */
class SoundManager(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val sounds = mutableMapOf<SoundType, Int>()

    enum class SoundType {
        TURN_COMPLETE, STEP, RESET, CONFIRM, POPUP, DISMISS
    }

    init {
        try {
            sounds[SoundType.TURN_COMPLETE] = soundPool.load(context, R.raw.goal_reached, 1)
            sounds[SoundType.STEP] = soundPool.load(context, R.raw.increment, 1)
            sounds[SoundType.RESET] = soundPool.load(context, R.raw.reset, 1)
            sounds[SoundType.CONFIRM] = soundPool.load(context, R.raw.decrement, 1)
            sounds[SoundType.POPUP] = soundPool.load(context, R.raw.floating_popup, 1)
            sounds[SoundType.DISMISS] = soundPool.load(context, R.raw.floating_dismiss, 1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playSound(type: SoundType, soundEnabled: Boolean = true, volume: Float = 1.0f) {
        if (!soundEnabled) return
        
        if (audioManager?.ringerMode != AudioManager.RINGER_MODE_NORMAL) return

        val soundId = sounds[type] ?: return
        val clampedVolume = volume.coerceIn(0f, 1f)
        try {
            soundPool.play(soundId, clampedVolume, clampedVolume, 1, 0, 1f)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        soundPool.release()
    }
}
