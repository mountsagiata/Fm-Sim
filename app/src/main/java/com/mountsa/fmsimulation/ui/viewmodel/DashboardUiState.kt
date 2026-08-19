package com.mountsa.fmsimulation.ui.viewmodel

import com.mountsa.fmsimulation.data.local.entities.*
import com.mountsa.fmsimulation.domain.model.MatchSession

data class DashboardUiState(
    val club: ClubEntity? = null,
    val standing: StandingEntity? = null,
    val nextMatch: MatchUiModel? = null,

    val inboxCount: Int = 0,
    val fixtures: List<MatchUiModel> = emptyList(),
    val allMatches: List<MatchEntity> = emptyList(),
    val allClubs: List<ClubEntity> = emptyList(),
    val topScorer: PlayerEntity? = null,
    val topAssister: PlayerEntity? = null,
    val bestPlayer: PlayerEntity? = null,
    val isLoading: Boolean = false,
    val loadingMessage: String = "Processing...",
    val currentDate: Long = 0L,
    val matchFlow: MatchFlow = MatchFlow.NONE,
    val matchSession: MatchSession? = null,
    val calendarEvents: List<CalendarEventEntity> = emptyList(),
    val inboxMessages: List<InboxEntity> = emptyList(),
    val selectedInboxMessage: InboxEntity? = null,
    val transferOffers: List<TransferOfferUiModel> = emptyList(),
    val objectives: List<ObjectiveEntity> = emptyList(),
    
    // Item 28: Scouting
    val scouts: List<ScoutEntity> = emptyList(),

    // Standing & League
    val leagueStandings: List<StandingEntity> = emptyList(),
    
    // Squad for Training/Match Hubs
    val squadPlayers: List<PlayerEntity> = emptyList()
)
