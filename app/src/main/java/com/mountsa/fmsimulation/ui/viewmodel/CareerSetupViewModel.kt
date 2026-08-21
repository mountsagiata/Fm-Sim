package com.mountsa.fmsimulation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mountsa.fmsimulation.data.local.entities.ClubEntity
import com.mountsa.fmsimulation.data.local.entities.LeagueEntity
import com.mountsa.fmsimulation.data.local.entities.NationEntity
import com.mountsa.fmsimulation.data.local.entities.SaveCareerEntity
import com.mountsa.fmsimulation.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class LeagueWithTeamCount(
    val league: LeagueEntity,
    val teamCount: Int
)

@HiltViewModel
class CareerSetupViewModel @Inject constructor(
    private val repository: DataRepository,
    private val databaseSeeder: com.mountsa.fmsimulation.core.managers.DatabaseSeeder
) : ViewModel() {

    private val _teamCountCache = MutableStateFlow<Map<Long, Int>>(emptyMap())

    private val _step = MutableStateFlow(0)
    val step = _step.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _generationMessage = MutableStateFlow("Preparing your career...")
    val generationMessage = _generationMessage.asStateFlow()

    private val _generationProgress = MutableStateFlow(0f)
    val generationProgress = _generationProgress.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedNation = MutableStateFlow<NationEntity?>(null)
    val selectedNation = _selectedNation.asStateFlow()

    private val _selectedLeague = MutableStateFlow<LeagueEntity?>(null)
    val selectedLeague = _selectedLeague.asStateFlow()

    private val _selectedClub = MutableStateFlow<ClubEntity?>(null)
    val selectedClub = _selectedClub.asStateFlow()

    // Nations filtered by search query AND only showing those with available leagues
    val nations = combine(
        repository.getAllNations(),
        repository.getAllLeagues(),
        _searchQuery
    ) { allNations, allLeagues, query ->
        val nationIdsWithLeagues = allLeagues.map { it.nationId }.toSet()
        val filtered = allNations.filter { it.id in nationIdsWithLeagues }
        
        if (query.isBlank()) filtered
        else filtered.filter { it.name.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Leagues filtered by selected nation
    val leagues = combine(
        repository.getAllLeagues(),
        _selectedNation,
        _teamCountCache
    ) { allLeagues, selectedNation, teamCounts ->
        if (selectedNation == null) emptyList()
        else allLeagues
            .filter { it.nationId == selectedNation.id }
            .map { LeagueWithTeamCount(it, teamCounts[it.id] ?: 0) }
            .sortedByDescending { it.teamCount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Clubs filtered by selected league
    val clubs = combine(
        repository.getAllClubs(),
        _selectedLeague
    ) { allClubs, selectedLeague ->
        if (selectedLeague == null) emptyList()
        else allClubs.filter { it.leagueId == selectedLeague.id }
            .sortedByDescending { it.overall }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadTeamCounts()
    }

    private fun loadTeamCounts() {
        viewModelScope.launch {
            repository.getAllClubs().collectLatest { clubs ->
                val teamCounts = clubs.groupingBy { it.leagueId }.eachCount()
                _teamCountCache.value = teamCounts
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun selectNation(nation: NationEntity) {
        _selectedNation.value = nation
        _selectedLeague.value = null
        _selectedClub.value = null
        _step.value = 1
    }

    fun selectLeague(league: LeagueEntity) {
        _selectedLeague.value = league
        _selectedClub.value = null
        _step.value = 2
    }

    fun selectClub(club: ClubEntity) {
        _selectedClub.value = club
        _step.value = 3
    }

    fun confirmCareer() {
        if (_loading.value) return
        val club = _selectedClub.value ?: return
        
        _loading.value = true
        viewModelScope.launch {
            try {
                _generationMessage.value = "Generating system core..."
                _generationProgress.value = 0.2f
                delay(800)

                _generationMessage.value = "Initializing League Standings..."
                _generationProgress.value = 0.4f
                repository.initializeLeagueStandings(club.leagueId)
                delay(600)

                _generationMessage.value = "Preparing Squad Data..."
                _generationProgress.value = 0.7f
                delay(600)

                _generationMessage.value = "Finalizing Career Data..."
                _generationProgress.value = 0.9f
                
                // NORMALIZE START DATE: August 1st, 2025
                val calendar = Calendar.getInstance()
                calendar.set(2025, Calendar.AUGUST, 1, 0, 0, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startTime = calendar.timeInMillis

                repository.saveCareer(
                    SaveCareerEntity(
                        managerId = 1,
                        selectedClubId = club.id,
                        selectedLeagueId = club.leagueId,
                        selectedNationId = club.nationId,
                        season = "2025/2026",
                        currentDate = startTime,
                        transferBudget = club.budget,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                )

                _generationMessage.value = "Generating Match Fixtures..."
                _generationProgress.value = 0.95f
                // Must run AFTER saveCareer() above, since fixture generation
                // reads the career's season to schedule matches correctly.
                databaseSeeder.seedFixtures()

                _generationProgress.value = 1.0f
                _generationMessage.value = "Welcome to ${club.name}!"
                delay(500)
                // Navigation is handled by MainViewModel observing the database
            } catch (e: Exception) {
                _loading.value = false
                _generationMessage.value = "Error: ${e.message}"
                e.printStackTrace()
            }
        }
    }
}
