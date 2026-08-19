package com.mountsa.fmsimulation

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mountsa.fmsimulation.core.match.commentary.CommentaryRepository
import com.mountsa.fmsimulation.ui.navigation.Screen
import com.mountsa.fmsimulation.ui.screens.career.CareerSetupScreen
import com.mountsa.fmsimulation.ui.screens.dashboard.DashboardScreen
import com.mountsa.fmsimulation.ui.screens.intro.IntroScreen
import com.mountsa.fmsimulation.ui.screens.match.MatchSimulationScreen
import com.mountsa.fmsimulation.ui.screens.profile.ProfileScreen
import com.mountsa.fmsimulation.ui.screens.splash.FmSplashScreen
import com.mountsa.fmsimulation.ui.theme.FootballManagerSimulationTheme
import com.mountsa.fmsimulation.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Load Commentary Templates
        CommentaryRepository.load(this)

        // 1. Enable Edge-to-Edge first
        enableEdgeToEdge()

        // 2. Force Landscape
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        // 3. Setup Immersive Mode (Hide bars)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        // 4. Handle Notch/Cutout
        window.attributes.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        setContent {
            FootballManagerSimulationTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val currentScreen by viewModel.screen.collectAsStateWithLifecycle()

                    when (currentScreen) {
                        Screen.Splash -> {
                            FmSplashScreen(hiltViewModel())
                        }
                        Screen.Intro -> {
                            IntroScreen(onNext = { viewModel.navigateFromIntro() })
                        }
                        Screen.Profile -> {
                            ProfileScreen(onContinue = { viewModel.navigateFromProfile() })
                        }
                        Screen.CareerSetup -> {
                            CareerSetupScreen()
                        }
                        Screen.Dashboard -> {
                            DashboardScreen()
                        }
                        Screen.MatchReveal -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("MATCH REVEAL", color = Color.White)
                            }
                        }
                        Screen.StartingLineup -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("STARTING LINEUP", color = Color.White)
                            }
                        }
                        Screen.MatchSimulation -> {
                            MatchSimulationScreen(hiltViewModel())
                        }
                        Screen.MatchResult -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("MATCH RESULT", color = Color.White)
                            }
                        }
                        Screen.PostMatch -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("POST MATCH", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
