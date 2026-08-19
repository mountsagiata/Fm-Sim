package com.mountsa.fmsimulation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mountsa.fmsimulation.core.enums.Mentality
import com.mountsa.fmsimulation.data.local.entities.ClubEntity
import com.mountsa.fmsimulation.data.local.entities.PlayerEntity
import com.mountsa.fmsimulation.data.repository.DataRepository
import com.mountsa.fmsimulation.domain.models.Formation
import com.mountsa.fmsimulation.domain.models.Formations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SquadViewModel @Inject constructor(
    private val repository: DataRepository
) : ViewModel() {

    private val _clubId = MutableStateFlow<Long?>(null)
    val clubId = _clubId.asStateFlow()

    private val _club = MutableStateFlow<ClubEntity?>(null)
    val club = _club.asStateFlow()

    val players = _clubId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getPlayersByClub(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedFormation = MutableStateFlow(Formations.DEFAULT_FORMATIONS[0])
    val selectedFormation = _selectedFormation.asStateFlow()

    private val _startingXI = MutableStateFlow<List<PlayerEntity?>>(List(11) { null })
    val startingXI = _startingXI.asStateFlow()

    private val _substitutes = MutableStateFlow<List<PlayerEntity?>>(List(7) { null })
    val substitutes = _substitutes.asStateFlow()

    private val _showPlayerSelector = MutableStateFlow(false)
    val showPlayerSelector = _showPlayerSelector.asStateFlow()

    private val _selectedPlayer = MutableStateFlow<PlayerEntity?>(null)
    val selectedPlayer = _selectedPlayer.asStateFlow()

    private var selectedSlotIndex: Int = -1

    init {
        viewModelScope.launch {
            _clubId.collect { id ->
                if (id != null) {
                    val currentClub = repository.getClubById(id)
                    _club.value = currentClub
                    
                    // Load current lineup
                    val squad = repository.getPlayersByClubSync(id)
                    val starters = MutableList<PlayerEntity?>(11) { null }
                    squad.filter { it.startingIndex in 0..10 }.forEach { 
                        starters[it.startingIndex] = it
                    }
                    _startingXI.value = starters
                }
            }
        }
    }

    fun setClubId(id: Long) {
        _clubId.value = id
    }

    fun selectFormation(formation: Formation) {
        _selectedFormation.value = formation
    }

    fun openPlayerSelector(index: Int) {
        selectedSlotIndex = index
        _showPlayerSelector.value = true
    }

    fun closePlayerSelector() {
        _showPlayerSelector.value = false
        selectedSlotIndex = -1
    }

    fun selectPlayerForPosition(player: PlayerEntity) {
        if (selectedSlotIndex != -1) {
            val currentStarters = _startingXI.value.toMutableList()
            
            // If player was already in XI, remove from old position
            val oldIndex = currentStarters.indexOfFirst { it?.id == player.id }
            if (oldIndex != -1) {
                currentStarters[oldIndex] = null
            }
            
            currentStarters[selectedSlotIndex] = player
            _startingXI.value = currentStarters
            closePlayerSelector()
        }
    }

    fun selectPlayer(player: PlayerEntity?) {
        _selectedPlayer.value = player
    }

    fun getAvailablePlayers(): List<PlayerEntity> {
        val starters = _startingXI.value.filterNotNull().map { it.id }.toSet()
        return players.value.filter { it.id !in starters }
    }

    fun moveToStartingXI(player: PlayerEntity) {
        val currentStarters = _startingXI.value.toMutableList()
        val emptySlot = currentStarters.indexOf(null)
        if (emptySlot != -1) {
            currentStarters[emptySlot] = player
            _startingXI.value = currentStarters
        }
    }

    fun moveToSubstitute(player: PlayerEntity) {
        // Basic logic for subs
    }

    /**
     * Item 9: Advanced Tactical Updates
     */
    fun updateTactics(
        mentality: Mentality? = null,
        tempo: Int? = null,
        pressing: Int? = null,
        dLine: Int? = null,
        width: Int? = null,
        directness: Int? = null
    ) {
        val currentClub = _club.value ?: return
        val updatedClub = currentClub.copy(
            mentality = mentality ?: currentClub.mentality,
            tempo = tempo ?: currentClub.tempo,
            pressing = pressing ?: currentClub.pressing,
            defensiveLine = dLine ?: currentClub.defensiveLine,
            attackingWidth = width ?: currentClub.attackingWidth,
            passingDirectness = directness ?: currentClub.passingDirectness
        )
        
        viewModelScope.launch {
            repository.updateClub(updatedClub)
            _club.value = updatedClub
        }
    }

    fun saveLineup() {
        viewModelScope.launch {
            val currentStarters = _startingXI.value
            val all = players.value
            
            all.forEach { p ->
                val startIndex = currentStarters.indexOfFirst { it?.id == p.id }
                val updatedPlayer = p.copy(startingIndex = startIndex)
                if (updatedPlayer != p) {
                    repository.updatePlayer(updatedPlayer)
                }
            }
        }
    }
}
