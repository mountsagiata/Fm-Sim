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

    private val _musicVolume = MutableStateFlow(prefs.getFloat(KEY_MUSIC_VOLUME, 0.4f))
    val musicVolume: StateFlow<Float> = _musicVolume

    private val _sfxVolume = MutableStateFlow(prefs.getFloat(KEY_SFX_VOLUME, 1f))
    val sfxVolume: StateFlow<Float> = _sfxVolume

    private val _crowdVolume = MutableStateFlow(prefs.getFloat(KEY_CROWD_VOLUME, 0.5f))
    val crowdVolume: StateFlow<Float> = _crowdVolume

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

    fun setMusicVolume(volume: Float) {
        val safeVolume = volume.coerceIn(0f, 1f)
        _musicVolume.value = safeVolume
        prefs.edit().putFloat(KEY_MUSIC_VOLUME, safeVolume).apply()
        musicPlayer?.setVolume(safeVolume, safeVolume)
    }

    fun setSfxVolume(volume: Float) {
        val safeVolume = volume.coerceIn(0f, 1f)
        _sfxVolume.value = safeVolume
        prefs.edit().putFloat(KEY_SFX_VOLUME, safeVolume).apply()
    }

    fun setCrowdVolume(volume: Float) {
        val safeVolume = volume.coerceIn(0f, 1f)
        _crowdVolume.value = safeVolume
        prefs.edit().putFloat(KEY_CROWD_VOLUME, safeVolume).apply()
        crowdPlayer?.setVolume(safeVolume, safeVolume)
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
        playSound(confirmSoundId)
    }

    /** For confirm / continue actions. */
    fun playConfirmSound() {
        if (!_sfxEnabled.value) return
        playSound(confirmSoundId)
    }

    /** For back / discard / cancel actions. */
    fun playBackSound() {
        if (!_sfxEnabled.value) return
        playSound(backSoundId)
    }

    /** For inbox / notification alerts. */
    fun playNotificationSound() {
        if (!_sfxEnabled.value) return
        playSound(notificationSoundId)
    }

    /** Kept for backward-compat call sites. */
    fun playSuccessSound() {
        if (!_sfxEnabled.value) return
        playSound(notificationSoundId)
    }

    private fun playSound(soundId: Int) {
        val volume = _sfxVolume.value
        soundPool.play(soundId, volume, volume, 0, 0, 1f)
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
            setVolume(_musicVolume.value, _musicVolume.value)
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
            setVolume(_crowdVolume.value, _crowdVolume.value)
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
        private const val KEY_MUSIC_VOLUME = "music_volume"
        private const val KEY_SFX_VOLUME = "sfx_volume"
        private const val KEY_CROWD_VOLUME = "crowd_volume"
    }
}
