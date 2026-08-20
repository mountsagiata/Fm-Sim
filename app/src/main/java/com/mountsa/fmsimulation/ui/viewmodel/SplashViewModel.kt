package com.mountsa.fmsimulation.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.mountsa.fmsimulation.core.managers.DatabaseSeeder
import com.mountsa.fmsimulation.utils.AudioManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val databaseSeeder: DatabaseSeeder,
    private val audioManager: AudioManager
) : ViewModel() {
    // Expose loading state and progress from the singleton seeder
    val loadingMessage = databaseSeeder.loadingMessage
    val progress = databaseSeeder.progress

    init {
        // Start background music as soon as the app is first opened.
        audioManager.playBackgroundMusic()
    }
}
