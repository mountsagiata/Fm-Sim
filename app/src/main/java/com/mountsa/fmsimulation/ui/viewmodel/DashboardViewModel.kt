package com.mountsa.fmsimulation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.mountsa.fmsimulation.data.local.entities.*
import com.mountsa.fmsimulation.data.repository.DataRepository
import com.mountsa.fmsimulation.domain.model.MatchSession
import com.mountsa.fmsimulation.domain.services.*
import com.mountsa.fmsimulation.domain.engine.LeagueManager
import com.mountsa.fmsimulation.domain.engine.TransferManager
import com.mountsa.fmsimulation.core.enums.InboxCategory
import com.mountsa.fmsimulation.core.enums.TransferStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MatchUiModel(
    val id: Long,
    val homeClubId: Long,
    val awayClubId: Long,
    val homeShortName: String,
    val awayShortName: String,
    val matchDate: Long,
    val leagueId: Long? = null,
    val leagueName: String = "",
    val isPlayed: Boolean = false,
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val opponentClubId: Long = -1L,
    val opponentShortName: String = "",
    val isHome: Boolean = true
)

data class TransferOfferUiModel(
    val offer: TransferOfferEntity,
    val playerName: String,
    val buyerClubName: String,
    val sellerClubName: String
)

enum class MatchFlow {
    NONE, REVEAL, LINEUP, SIMULATION, RESULT, POST
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DataRepository,
    private val processor: GameDayProcessor,
    private val matchSimulator: MatchSimulator,
    private val sessionBuilder: MatchSessionBuilder,
    private val leagueManager: LeagueManager,
    private val transferManager: TransferManager,
    private val managerRatingService: ManagerRatingService,
    private val pressConferenceGenerator: PressConferenceGenerator,
    val audioManager: com.mountsa.fmsimulation.utils.AudioManager,
    val localeManager: com.mountsa.fmsimulation.utils.LocaleManager
) : ViewModel() {

    val allPlayers: StateFlow<List<PlayerEntity>> = repository.getAllPlayers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val gson = Gson()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadingMessage = MutableStateFlow("Processing...")
    val loadingMessage: StateFlow<String> = _loadingMessage.asStateFlow()

    private val _matchFlowState = MutableStateFlow(MatchFlow.NONE)
    val matchFlowState: StateFlow<MatchFlow> = _matchFlowState.asStateFlow()

    private val _matchSession = MutableStateFlow<MatchSession?>(null)
    val matchSession: StateFlow<MatchSession?> = _matchSession.asStateFlow()

    // League Stats States
    private val _topScorer = MutableStateFlow<PlayerEntity?>(null)
    private val _topAssister = MutableStateFlow<PlayerEntity?>(null)
    private val _bestPlayer = MutableStateFlow<PlayerEntity?>(null)

    val career: StateFlow<SaveCareerEntity?> = repository.getCareer().stateIn(
        viewModelScope, SharingStarted.Eagerly, null
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val club: StateFlow<ClubEntity?> = career.flatMapLatest { c ->
        if (c == null) flowOf<ClubEntity?>(null)
        else flow<ClubEntity?> { emit(repository.getClubById(c.selectedClubId)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val allClubs: StateFlow<List<ClubEntity>> = repository.getAllClubs().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val squadPlayers: StateFlow<List<PlayerEntity>> = club.flatMapLatest { cl ->
        if (cl == null) flowOf(emptyList())
        else repository.getPlayersByClub(cl.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val clubStanding: StateFlow<StandingEntity?> = club.flatMapLatest { cl ->
        if (cl == null) flowOf<StandingEntity?>(null)
        else repository.getLeagueStandings(cl.leagueId).map { list ->
            list.find { it.clubId == cl.id }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val leagueStandings: StateFlow<List<StandingEntity>> = club.flatMapLatest { cl ->
        if (cl == null) flowOf(emptyList())
        else repository.getLeagueStandings(cl.leagueId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val leagueName: StateFlow<String> = club.flatMapLatest { cl ->
        if (cl == null) flowOf("")
        else flow { emit(repository.getLeagueName(cl.leagueId)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    @OptIn(ExperimentalCoroutinesApi::class)
    val nextMatch: StateFlow<MatchUiModel?> = club.flatMapLatest { cl ->
        if (cl == null) flowOf(null)
        else repository.getNextMatchFlow(cl.id).map { match ->
            if (match == null) null
            else {
                val homeClub = repository.getClubById(match.homeClubId)
                val awayClub = repository.getClubById(match.awayClubId)
                val isHome = match.homeClubId == cl.id
                MatchUiModel(
                    id = match.id,
                    homeClubId = match.homeClubId,
                    awayClubId = match.awayClubId,
                    homeShortName = homeClub?.shortName ?: "T1",
                    awayShortName = awayClub?.shortName ?: "T2",
                    matchDate = match.matchDate,
                    leagueId = match.leagueId,
                    leagueName = repository.getLeagueName(match.leagueId ?: -1),
                    opponentClubId = if (isHome) match.awayClubId else match.homeClubId,
                    opponentShortName = if (isHome) (awayClub?.shortName ?: "T2") else (homeClub?.shortName ?: "T1"),
                    isHome = isHome,
                    isPlayed = match.isPlayed
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val upcomingFixtures: StateFlow<List<MatchUiModel>> = combine(club, career) { cl, cr -> cl to cr }
        .flatMapLatest { (cl, cr) ->
            if (cl == null || cr == null) flowOf(emptyList())
            else repository.getMatchesForClub(cl.id).map { matches ->
                matches
                    .filter { it.matchDate >= cr.currentDate && !it.isPlayed }
                    .sortedBy { it.matchDate }
                    .take(10)
                    .map { m ->
                        val isHome = m.homeClubId == cl.id
                        val oppId = if (isHome) m.awayClubId else m.homeClubId
                        MatchUiModel(
                            id = m.id,
                            homeClubId = m.homeClubId,
                            awayClubId = m.awayClubId,
                            homeShortName = "",
                            awayShortName = "",
                            matchDate = m.matchDate,
                            opponentClubId = oppId,
                            opponentShortName = "Opp",
                            isHome = isHome
                        )
                    }
            }
        }
        .flatMapLatest { list ->
            flow {
                val enriched = list.map { m ->
                    val opp = repository.getClubById(m.opponentClubId)
                    m.copy(opponentShortName = opp?.shortName ?: "UNK")
                }
                emit(enriched)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val allMatches: StateFlow<List<MatchEntity>> = club.flatMapLatest { cl ->
        if (cl == null) flowOf(emptyList())
        else repository.getMatchesForClub(cl.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val inboxCount: StateFlow<Int> = club.flatMapLatest { cl ->
        if (cl == null) flowOf(0)
        else repository.getUnreadInboxCount(cl.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val calendarEvents: StateFlow<List<CalendarEventEntity>> = club.flatMapLatest { cl ->
        if (cl == null) flowOf(emptyList())
        else repository.getCalendar(cl.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val inboxMessages: StateFlow<List<InboxEntity>> = club.flatMapLatest { cl ->
        if (cl == null) flowOf(emptyList())
        else repository.getInbox(cl.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedInboxMessage = MutableStateFlow<InboxEntity?>(null)
    val selectedInboxMessage: StateFlow<InboxEntity?> = _selectedInboxMessage.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val transferOffers: StateFlow<List<TransferOfferUiModel>> = club.flatMapLatest { cl ->
        if (cl == null) flowOf(emptyList())
        else repository.getOffersForClub(cl.id).flatMapLatest { offers ->
            flow {
                val uiModels = offers.map { offer ->
                    val player = repository.getPlayerById(offer.playerId)
                    val buyer = repository.getClubById(offer.buyerClubId)
                    val seller = repository.getClubById(offer.sellerClubId)
                    TransferOfferUiModel(
                        offer = offer,
                        playerName = player?.name ?: "Unknown Player",
                        buyerClubName = buyer?.name ?: "Unknown Club",
                        sellerClubName = seller?.name ?: "Unknown Club"
                    )
                }
                emit(uiModels)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val objectives: StateFlow<List<ObjectiveEntity>> = club.flatMapLatest { cl ->
        if (cl == null) flowOf(emptyList())
        else repository.getObjectives(cl.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val scouts: StateFlow<List<ScoutEntity>> = club.flatMapLatest { cl ->
        if (cl == null) flowOf(emptyList())
        else repository.getScoutsForClub(cl.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState = combine(
        club,
        clubStanding,
        nextMatch,
        inboxCount,
        upcomingFixtures,
        allMatches,
        allClubs,
        _topScorer,
        _topAssister,
        _bestPlayer,
        _isLoading,
        _loadingMessage,
        career,
        _matchFlowState,
        _matchSession,
        calendarEvents,
        inboxMessages,
        selectedInboxMessage,
        transferOffers,
        objectives,
        scouts,
        leagueStandings,
        squadPlayers
    ) { args ->
        val cr = args[12] as SaveCareerEntity?
        DashboardUiState(
            club = args[0] as ClubEntity?,
            standing = args[1] as StandingEntity?,
            nextMatch = args[2] as MatchUiModel?,
            inboxCount = args[3] as Int,
            fixtures = @Suppress("UNCHECKED_CAST") (args[4] as List<MatchUiModel>),
            allMatches = @Suppress("UNCHECKED_CAST") (args[5] as List<MatchEntity>),
            allClubs = @Suppress("UNCHECKED_CAST") (args[6] as List<ClubEntity>),
            topScorer = args[7] as PlayerEntity?,
            topAssister = args[8] as PlayerEntity?,
            bestPlayer = args[9] as PlayerEntity?,
            isLoading = args[10] as Boolean,
            loadingMessage = args[11] as String,
            currentDate = cr?.currentDate ?: 0L,
            matchFlow = args[13] as MatchFlow,
            matchSession = args[14] as MatchSession?,
            calendarEvents = @Suppress("UNCHECKED_CAST") (args[15] as List<CalendarEventEntity>),
            inboxMessages = @Suppress("UNCHECKED_CAST") (args[16] as List<InboxEntity>),
            selectedInboxMessage = args[17] as InboxEntity?,
            transferOffers = @Suppress("UNCHECKED_CAST") (args[18] as List<TransferOfferUiModel>),
            objectives = @Suppress("UNCHECKED_CAST") (args[19] as List<ObjectiveEntity>),
            scouts = @Suppress("UNCHECKED_CAST") (args[20] as List<ScoutEntity>),
            leagueStandings = @Suppress("UNCHECKED_CAST") (args[21] as List<StandingEntity>),
            squadPlayers = @Suppress("UNCHECKED_CAST") (args[22] as List<PlayerEntity>)
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardUiState()
    )

    init {
        observeLeagueStats()
    }

    private fun observeLeagueStats() {
        viewModelScope.launch {
            club.collectLatest { cl ->
                if (cl != null) {
                    launch { repository.getTopScorersByLeague(cl.leagueId, 1).collect { _topScorer.value = it.firstOrNull() } }
                    launch { repository.getTopAssistsByLeague(cl.leagueId, 1).collect { _topAssister.value = it.firstOrNull() } }
                    launch { repository.getTopPlayersByLeague(cl.leagueId, 1).collect { _bestPlayer.value = it.firstOrNull() } }
                }
            }
        }
    }

    /**
     * GERBANG UTAMA LANJUT HARI
     */
    fun onContinueClick() {
        if (_isLoading.value) return

        viewModelScope.launch {
            try {
                val currentCareer = career.value ?: return@launch

                // 1. Cek apakah hari ini ada match untuk user
                val todayMatch = repository.getTodayUserMatch(
                    currentCareer.selectedClubId, 
                    currentCareer.currentDate
                )

                if (todayMatch != null && !todayMatch.isPlayed) {
                    // ADA MATCH HARI INI -> Masuk Flow Pertandingan
                    beginMatchFlow(todayMatch)
                } else {
                    // TIDAK ADA MATCH -> Jalankan Simulasi Maju Hari
                    _isLoading.value = true
                    _loadingMessage.value = "Advancing to next day..."

                    // Delay buatan agar user melihat loading (Efek FM)
                    delay(500)

                    processor.continueDay()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun beginMatchFlow(matchEntity: MatchEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            _loadingMessage.value = "Preparing Matchday..."
            delay(800) // Delay persiapan
            
            try {
                val session = sessionBuilder.build(matchEntity)
                _matchSession.value = session
                _matchFlowState.value = MatchFlow.REVEAL
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun nextMatchFlowStep() {
        val currentState = _matchFlowState.value
        viewModelScope.launch {
            when (currentState) {
                MatchFlow.REVEAL -> _matchFlowState.value = MatchFlow.LINEUP
                MatchFlow.LINEUP -> {
                    takeMatchSnapshot()
                    // Simulasi hasil di balik layar sebelum masuk ke layar SIMULATION
                    simulateUserMatch()
                    _matchFlowState.value = MatchFlow.SIMULATION
                }
                MatchFlow.SIMULATION -> {
                    _matchFlowState.value = MatchFlow.RESULT
                }
                MatchFlow.RESULT -> _matchFlowState.value = MatchFlow.POST
                MatchFlow.POST -> {
                    finishMatchFlow()
                }
                else -> _matchFlowState.value = MatchFlow.NONE
            }
        }
    }

    private suspend fun takeMatchSnapshot() {
        val session = _matchSession.value ?: return
        val homeLineupJson = gson.toJson(session.homeLineup)
        val awayLineupJson = gson.toJson(session.awayLineup)
        val updatedMatch = session.match.copy(
            homeLineupJson = homeLineupJson,
            awayLineupJson = awayLineupJson
        )
        repository.updateMatch(updatedMatch)
        _matchSession.value = session.copy(match = updatedMatch)
    }

    private suspend fun simulateUserMatch() {
        val session = _matchSession.value ?: return
        _isLoading.value = true
        _loadingMessage.value = "Calculating Tactics..."
        delay(1000)
        try {
            matchSimulator.simulateMatch(session.match)
            val updatedMatch = repository.getMatchById(session.match.id)
            if (updatedMatch != null) {
                _matchSession.value = session.copy(match = updatedMatch)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isLoading.value = false
        }
    }

    fun resetCareer() {
        viewModelScope.launch {
            audioManager.stopBackgroundMusic()
            audioManager.stopCrowdAmbience()
            repository.resetCareerData()
            // MainViewModel's career observer will detect career == null
            // and automatically navigate back to the Profile screen.
        }
    }

    fun swapMatchPlayer(starterId: Long, substituteId: Long) {
        val session = _matchSession.value ?: return
        val userIsHome = career.value?.selectedClubId == session.match.homeClubId
        val currentLineup = if (userIsHome) session.homeLineup else session.awayLineup
        val currentBench = if (userIsHome) session.homeBench else session.awayBench
        val starter = currentLineup.firstOrNull { it.id == starterId } ?: return
        val substitute = currentBench.firstOrNull { it.id == substituteId } ?: return
        val starterIndex = currentLineup.indexOfFirst { it.id == starterId }
        val substituteIndex = currentBench.indexOfFirst { it.id == substituteId }
        if (starterIndex < 0 || substituteIndex < 0) return

        val lineup = currentLineup.toMutableList().apply { set(starterIndex, substitute) }
        val bench = currentBench.toMutableList().apply { set(substituteIndex, starter) }
        _matchSession.value = if (userIsHome) session.copy(homeLineup = lineup, homeBench = bench)
        else session.copy(awayLineup = lineup, awayBench = bench)
    }

    fun finishMatchFlow() {
        val session = _matchSession.value
        if (_isLoading.value) return // guard against double-tap re-entry
        viewModelScope.launch {
            try {
                if (session != null) {
                    _isLoading.value = true
                    _loadingMessage.value = "Finalizing Match Results..."
                    
                    // 1. Update Standing Positions
                    session.match.leagueId?.let { leagueId ->
                        leagueManager.updateStandings(leagueId)
                    }
                    
                    // 2. Update Manager Rating
                    managerRatingService.updateManagerRating(session.match.homeClubId)
                    
                    // 3. Post-Match Press Conference
                    pressConferenceGenerator.generatePressConference(
                        club.value?.id ?: session.match.homeClubId,
                        PressType.POST_MATCH
                    )
                    
                    // 4. Generate Inbox Result
                    generateMatchResultInbox(session.match)
                    
                    delay(500)

                    // 5. Process the rest of the day: this simulates the OTHER league
                    // matches scheduled on the same date (AI vs AI), runs training /
                    // injury / morale / transfer updates, and advances to the next day.
                    // Without this, other clubs never play on days the user has a
                    // match, so their standings stay stuck at 0.
                    _loadingMessage.value = "Processing other results..."
                    processor.continueDay()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // Always reached, even if something above threw — the user is
                // never left stuck on the post-match screen.
                _matchFlowState.value = MatchFlow.NONE
                _matchSession.value = null
                _isLoading.value = false
            }
        }
    }

    private suspend fun generateMatchResultInbox(match: MatchEntity) {
        val clubId = career.value?.selectedClubId ?: return
        val isHome = match.homeClubId == clubId
        val opponentId = if (isHome) match.awayClubId else match.homeClubId
        val opponentName = repository.getClubById(opponentId)?.name ?: "Opponent"
        
        val resultType = when {
            match.homeScore == match.awayScore -> "Draw"
            isHome && match.homeScore > match.awayScore -> "Win"
            !isHome && match.awayScore > match.homeScore -> "Win"
            else -> "Loss"
        }
        
        val scoreText = "${match.homeScore}-${match.awayScore}"
        val subject = "Match Result: $resultType vs $opponentName"
        val message = "The match against $opponentName has ended with a score of $scoreText. " +
                "Check the standings to see your current position."

        repository.addInbox(
            InboxEntity(
                clubId = clubId,
                sender = "Competition Board",
                subject = subject,
                message = message,
                category = InboxCategory.MATCH,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    fun selectInboxMessage(message: InboxEntity) {
        _selectedInboxMessage.value = message
        if (!message.isRead) {
            viewModelScope.launch {
                repository.markInboxAsRead(message.id)
            }
        }
    }

    fun updateTransferOfferStatus(offer: TransferOfferEntity, status: TransferStatus) {
        viewModelScope.launch {
            val updatedOffer = offer.copy(status = status)
            repository.updateOffer(updatedOffer)
            
            // IF ACCEPTED -> Process the actual transfer (Pipeline Connect)
            if (status == TransferStatus.ACCEPTED) {
                _isLoading.value = true
                _loadingMessage.value = "Finalizing Transfer Documents..."
                delay(1000)
                transferManager.processTransfer(updatedOffer)
                _isLoading.value = false
            }
        }
    }
}
