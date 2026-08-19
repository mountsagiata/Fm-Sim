package com.mountsa.fmsimulation.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.mountsa.fmsimulation.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // --- SHORT SFX (SoundPool) ---
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(audioAttributes)
        .build()

    private val confirmSoundId = soundPool.load(context, R.raw.confirm, 1)
    private val backSoundId = soundPool.load(context, R.raw.back, 1)
    private val notificationSoundId = soundPool.load(context, R.raw.notification, 1)

    /** Generic click sound for buttons / navigation (uses confirm.ogg). */
    fun playClickSound() {
        soundPool.play(confirmSoundId, 1f, 1f, 0, 0, 1f)
    }

    /** For confirm / continue actions. */
    fun playConfirmSound() {
        soundPool.play(confirmSoundId, 1f, 1f, 0, 0, 1f)
    }

    /** For back / discard / cancel actions. */
    fun playBackSound() {
        soundPool.play(backSoundId, 1f, 1f, 0, 0, 1f)
    }

    /** For inbox / notification alerts. */
    fun playNotificationSound() {
        soundPool.play(notificationSoundId, 1f, 1f, 0, 0, 1f)
    }

    /** Kept for backward-compat call sites. */
    fun playSuccessSound() {
        soundPool.play(notificationSoundId, 1f, 1f, 0, 0, 1f)
    }

    // --- LONG-FORM AUDIO (MediaPlayer): background music & crowd ambience ---
    private var musicPlayer: MediaPlayer? = null
    private var crowdPlayer: MediaPlayer? = null
    private var currentTrack: Int = 0

    /**
     * Starts looping background music. Alternates between Soundtrack1 and
     * Soundtrack2 each time it's (re)started from a stopped state.
     */
    fun playBackgroundMusic() {
        if (musicPlayer?.isPlaying == true) return
        stopBackgroundMusic()
        currentTrack = if (currentTrack == 0) R.raw.soundtrack1 else R.raw.soundtrack2
        musicPlayer = MediaPlayer.create(context, currentTrack)?.apply {
            isLooping = true
            setVolume(0.4f, 0.4f)
            start()
        }
    }

    fun stopBackgroundMusic() {
        musicPlayer?.release()
        musicPlayer = null
    }

    /** Looping crowd ambience, used during live match simulation. */
    fun playCrowdAmbience() {
        if (crowdPlayer?.isPlaying == true) return
        crowdPlayer = MediaPlayer.create(context, R.raw.crowd1)?.apply {
            isLooping = true
            setVolume(0.5f, 0.5f)
            start()
        }
    }

    fun stopCrowdAmbience() {
        crowdPlayer?.release()
        crowdPlayer = null
    }

    fun release() {
        soundPool.release()
        stopBackgroundMusic()
        stopCrowdAmbience()
    }
}
