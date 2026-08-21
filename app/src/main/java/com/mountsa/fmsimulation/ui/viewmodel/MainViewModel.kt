package com.mountsa.fmsimulation.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mountsa.fmsimulation.core.managers.DatabaseSeeder
import com.mountsa.fmsimulation.data.repository.DataRepository
import com.mountsa.fmsimulation.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.mountsa.fmsimulation.domain.model.MatchSession

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: DataRepository,
    private val seeder: DatabaseSeeder
) : ViewModel() {

    private val _screen = MutableStateFlow<Screen>(Screen.Intro)
    val screen = _screen.asStateFlow()

    private val _matchSession = MutableStateFlow<MatchSession?>(null)
    val matchSession = _matchSession.asStateFlow()

    val loadingMessage = seeder.loadingMessage

    init {
        observeCareer()
    }

    private fun observeCareer() {
        viewModelScope.launch {
            repository.getCareer().collect { career ->
                val current = _screen.value
                // Only auto-advance while actively completing a NEW career setup.
                // Do NOT auto-skip the Profile screen just because a save already
                // exists — the user should see it and explicitly press
                // "CONTINUE CAREER" (or start a new one) instead of being
                // dropped straight into the dashboard.
                if (career != null && current == Screen.CareerSetup) {
                    _screen.value = Screen.Dashboard
                } else if (career == null && current == Screen.Dashboard) {
                    // Career was wiped (e.g. RESET CAREER DATA) — go back to Profile.
                    _screen.value = Screen.Profile
                }
            }
        }
    }

    fun navigateFromIntro() {
        _screen.value = Screen.Splash
        viewModelScope.launch {
            seeder.seedIfNeeded()
            
            // Check Checkpoint from metadata
            val checkpoint = repository.getMetadata("CAREER_FLOW_STEP")
            val career = repository.getCareer().first()
            
            _screen.value = when {
                // Even if a save already exists, go to Profile first so the
                // user can see it and choose CONTINUE CAREER / START NEW.
                career != null -> Screen.Profile
                checkpoint == "TEAM_SELECTION" -> Screen.CareerSetup
                else -> Screen.Profile
            }
        }
    }

    fun navigateFromProfile() {
        viewModelScope.launch {
            val career = repository.getCareer().first()
            val checkpoint = repository.getMetadata("CAREER_FLOW_STEP")
            
            _screen.value = when {
                career != null -> Screen.Dashboard
                checkpoint == "TEAM_SELECTION" -> Screen.CareerSetup
                else -> Screen.CareerSetup // Default for new start
            }
        }
    }

    fun openMatchFlow(session: MatchSession) {
        _matchSession.value = session
        _screen.value = Screen.MatchReveal
    }

    fun goToStartingLineup() {
        _screen.value = Screen.StartingLineup
    }

    fun goToSimulation() {
        _screen.value = Screen.MatchSimulation
    }

    fun goToResult() {
        _screen.value = Screen.MatchResult
    }

    fun goToPostMatch() {
        _screen.value = Screen.PostMatch
    }

    fun returnToDashboard() {
        _matchSession.value = null
        _screen.value = Screen.Dashboard
    }
}
