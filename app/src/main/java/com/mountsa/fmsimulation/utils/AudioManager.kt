package com.mountsa.fmsimulation.utils

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun playClickSound() {
        // Implementation for playing click sound
    }

    fun playSuccessSound() {
        // Implementation for playing success sound
    }
}
