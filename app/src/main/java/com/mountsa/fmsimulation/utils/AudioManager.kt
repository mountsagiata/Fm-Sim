package com.mountsa.fmsimulation.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.mountsa.fmsimulation.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("audio_settings", Context.MODE_PRIVATE)

    private val _musicEnabled = MutableStateFlow(prefs.getBoolean(KEY_MUSIC, true))
    val musicEnabled: StateFlow<Boolean> = _musicEnabled

    private val _sfxEnabled = MutableStateFlow(prefs.getBoolean(KEY_SFX, true))
    val sfxEnabled: StateFlow<Boolean> = _sfxEnabled

    fun setMusicEnabled(enabled: Boolean) {
        _musicEnabled.value = enabled
        prefs.edit().putBoolean(KEY_MUSIC, enabled).apply()
        if (!enabled) {
            stopBackgroundMusic()
            stopCrowdAmbience()
        } else {
            playBackgroundMusic()
        }
    }

    fun setSfxEnabled(enabled: Boolean) {
        _sfxEnabled.value = enabled
        prefs.edit().putBoolean(KEY_SFX, enabled).apply()
    }

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
        if (!_sfxEnabled.value) return
        soundPool.play(confirmSoundId, 1f, 1f, 0, 0, 1f)
    }

    /** For confirm / continue actions. */
    fun playConfirmSound() {
        if (!_sfxEnabled.value) return
        soundPool.play(confirmSoundId, 1f, 1f, 0, 0, 1f)
    }

    /** For back / discard / cancel actions. */
    fun playBackSound() {
        if (!_sfxEnabled.value) return
        soundPool.play(backSoundId, 1f, 1f, 0, 0, 1f)
    }

    /** For inbox / notification alerts. */
    fun playNotificationSound() {
        if (!_sfxEnabled.value) return
        soundPool.play(notificationSoundId, 1f, 1f, 0, 0, 1f)
    }

    /** Kept for backward-compat call sites. */
    fun playSuccessSound() {
        if (!_sfxEnabled.value) return
        soundPool.play(notificationSoundId, 1f, 1f, 0, 0, 1f)
    }

    // --- LONG-FORM AUDIO (MediaPlayer): background music & crowd ambience ---
    private var musicPlayer: MediaPlayer? = null
    private var crowdPlayer: MediaPlayer? = null
    private var currentTrack: Int = 0

    /**
     * Starts looping background music. Alternates between Soundtrack1 and
     * Soundtrack2 each time it's (re)started from a stopped state. No-op if
     * the user has music disabled in Settings.
     */
    fun playBackgroundMusic() {
        if (!_musicEnabled.value) return
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
        if (!_musicEnabled.value) return
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

    companion object {
        private const val KEY_MUSIC = "music_enabled"
        private const val KEY_SFX = "sfx_enabled"
    }
}
