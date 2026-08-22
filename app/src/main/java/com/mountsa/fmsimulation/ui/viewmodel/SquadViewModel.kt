package com.mountsa.fmsimulation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
    private val gson = Gson()

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

    private val _tacticalRoles = MutableStateFlow<Map<Long, String>>(emptyMap())
    val tacticalRoles = _tacticalRoles.asStateFlow()

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
                    _tacticalRoles.value = repository.getMetadata(tacticalRoleKey(id))
                        ?.let { json ->
                            runCatching {
                                gson.fromJson<Map<Long, String>>(
                                    json,
                                    object : TypeToken<Map<Long, String>>() {}.type
                                )
                            }.getOrNull()
                        }
                        .orEmpty()
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

    fun moveStartingPlayer(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in 0..10 || toIndex !in 0..10 || fromIndex == toIndex) return
        val lineup = _startingXI.value.toMutableList()
        val moved = lineup[fromIndex]
        lineup[fromIndex] = lineup[toIndex]
        lineup[toIndex] = moved
        _startingXI.value = lineup
    }

    fun moveFormationPosition(index: Int, xPercent: Float, yPercent: Float) {
        val current = _selectedFormation.value
        if (index !in current.positions.indices) return
        val positions = current.positions.toMutableList()
        positions[index] = positions[index].copy(
            x = xPercent.coerceIn(7f, 93f),
            y = yPercent.coerceIn(7f, 93f)
        )
        _selectedFormation.value = current.copy(name = "Custom", positions = positions)
    }

    fun autoFillLineup() {
        val available = players.value
            .filter { it.status.name == "FIT" }
            .toMutableList()
        val selected = MutableList<PlayerEntity?>(11) { null }
        _selectedFormation.value.positions.forEachIndexed { index, slot ->
            val best = available.maxByOrNull { player -> positionFit(player, slot.name) }
            selected[index] = best
            if (best != null) available.remove(best)
        }
        _startingXI.value = selected
    }

    private fun positionFit(player: PlayerEntity, slot: String): Int {
        val positions = (listOf(player.position) + player.secondaryPosition.split(",", "/"))
            .map { it.trim().uppercase() }
        val target = slot.uppercase()
        val exact = if (target in positions) 30 else 0
        val lineFit = when (target) {
            "GK" -> if (player.position == "GK") 25 else -80
            "CB", "LB", "RB", "LWB", "RWB" -> player.defending / 3
            "CDM", "CM", "CAM", "LM", "RM" -> (player.passing + player.vision + player.stamina) / 9
            else -> (player.shooting + player.dribbling + player.pace) / 9
        }
        return player.overall + exact + lineFit + player.fitness / 10 + player.morale / 10
    }

    fun updateTacticalRole(playerId: Long, role: String) {
        val updated = _tacticalRoles.value + (playerId to role)
        _tacticalRoles.value = updated
        _clubId.value?.let { id ->
            viewModelScope.launch {
                repository.saveMetadata(tacticalRoleKey(id), gson.toJson(updated))
            }
        }
    }

    private fun tacticalRoleKey(clubId: Long) = "TACTICAL_ROLES_$clubId"

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
        _club.value = updatedClub
        viewModelScope.launch {
            repository.updateClub(updatedClub)
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
