package com.mountsa.fmsimulation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mountsa.fmsimulation.data.local.entities.UserProfileEntity
import com.mountsa.fmsimulation.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: DataRepository
) : ViewModel() {

    val profiles: StateFlow<List<UserProfileEntity>> = repository.getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedProfile = MutableStateFlow<UserProfileEntity?>(null)
    val selectedProfile = _selectedProfile.asStateFlow()

    private val _hasSave = MutableStateFlow(false)
    val hasSave = _hasSave.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getCareer().collect { career ->
                _hasSave.value = career != null
            }
        }
    }

    fun selectProfile(profile: UserProfileEntity) {
        _selectedProfile.value = profile
        viewModelScope.launch {
            repository.saveMetadata("ACTIVE_PROFILE_ID", profile.id.toString())
            repository.saveMetadata("ACTIVE_MANAGER_NAME", profile.name)
        }
    }

    fun createProfile(name: String, avatarUri: String?) {
        viewModelScope.launch {
            val newProfile = UserProfileEntity(name = name, avatarUri = avatarUri)
            val id = repository.insertProfile(newProfile)
            val created = newProfile.copy(id = id)
            _selectedProfile.value = created
            
            // Save active profile to metadata
            repository.saveMetadata("ACTIVE_PROFILE_ID", id.toString())
            repository.saveMetadata("ACTIVE_MANAGER_NAME", name)
        }
    }

    fun deleteProfile(profile: UserProfileEntity) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
            if (_selectedProfile.value?.id == profile.id) {
                _selectedProfile.value = null
            }
        }
    }

    fun startNewCareer(onNext: () -> Unit) {
        viewModelScope.launch {
            // Checkpoint: Player has a profile and is now moving to team selection
            repository.saveMetadata("CAREER_FLOW_STEP", "TEAM_SELECTION")
            onNext()
        }
    }

    fun continueCareer(onNext: () -> Unit) {
        viewModelScope.launch {
            val career = repository.getCareer().first()
            if (career != null) {
                onNext()
            }
        }
    }
}
