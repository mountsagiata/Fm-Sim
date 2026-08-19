package com.mountsa.fmsimulation.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.mountsa.fmsimulation.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(audioAttributes)
        .build()

    private val clickSoundId = soundPool.load(context, R.raw.confirm, 1)
    private val successSoundId = soundPool.load(context, R.raw.notification, 1)
    private val backSoundId = soundPool.load(context, R.raw.back, 1)

    fun playClickSound() {
        soundPool.play(clickSoundId, 1f, 1f, 0, 0, 1f)
    }

    fun playSuccessSound() {
        soundPool.play(successSoundId, 1f, 1f, 0, 0, 1f)
    }

    fun playBackSound() {
        soundPool.play(backSoundId, 1f, 1f, 0, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}
