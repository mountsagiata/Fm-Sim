package com.mountsa.fmsimulation.domain.services

import android.util.Log
import com.mountsa.fmsimulation.core.enums.InboxCategory
import com.mountsa.fmsimulation.data.local.entities.InboxEntity
import com.mountsa.fmsimulation.data.local.entities.SaveCareerEntity
import com.mountsa.fmsimulation.data.repository.DataRepository
import com.mountsa.fmsimulation.domain.engine.ClubManager
import com.mountsa.fmsimulation.domain.engine.LeagueManager
import com.mountsa.fmsimulation.domain.engine.TrainingManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameDayProcessor @Inject constructor(
    private val repository: DataRepository,
    private val simulator: MatchSimulator,
    private val trainingManager: TrainingManager,
    private val injuryService: InjuryService,
    private val leagueManager: LeagueManager,
    private val pressConferenceGenerator: PressConferenceGenerator,
    private val managerRatingService: ManagerRatingService,
    private val transferOfferGenerator: TransferOfferGenerator,
    private val transferRumorService: TransferRumorService,
    private val clubManager: ClubManager,
    private val moraleService: MoraleService,
    private val aiTransferEngine: AITransferEngine,
    private val contractService: ContractService,
    private val calendarService: CalendarService,
    private val financialService: FinancialService
) {

    suspend fun continueDay() = withContext(Dispatchers.IO) {
        val career = repository.getCareer().firstOrNull() ?: return@withContext
        val clubId = career.selectedClubId
        val club = repository.getClubById(clubId) ?: return@withContext

        val currentDate = career.currentDate
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentDate
        
        Log.d("GameDayProcessor", "Processing day: $currentDate on ${Thread.currentThread().name}")

        trainingManager.processDailyTraining(clubId)
        injuryService.processDailyInjuries(clubId)
        moraleService.updateDailyMorale(clubId)

        calendarService.automateCalendar(clubId, currentDate)

        handleTransferWindowNotifications(career, calendar)
        
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val transferSimulationDay = dayOfWeek == Calendar.MONDAY || dayOfWeek == Calendar.THURSDAY
        if (isTransferWindowOpen(currentDate) && transferSimulationDay) {
            transferRumorService.generateRumors(clubId)
            transferOfferGenerator.generateRandomOffers(clubId)
            aiTransferEngine.processAIClubsTransfers()
        }
        if (isTransferWindowOpen(currentDate) && isDeadlineDay(calendar)) {
            pressConferenceGenerator.generatePressConference(clubId, PressType.TRANSFER)
        }

        val todaysMatches = repository.getMatchesByDate(currentDate).filter { !it.isPlayed }

        if (todaysMatches.isNotEmpty()) {
            val isUserMatchDay = todaysMatches.any { it.homeClubId == clubId || it.awayClubId == clubId }
            
            if (isUserMatchDay) {
                pressConferenceGenerator.generatePressConference(clubId, PressType.PRE_MATCH)
            }

            // Fixtures are independent until the table aggregation below. Running
            // them concurrently keeps every event/injury/result but removes the
            // long serial post-match wait on leagues with many matches.
            coroutineScope {
                todaysMatches
                    .filterNot { it.homeClubId == clubId || it.awayClubId == clubId }
                    .map { match -> async { simulator.simulateMatch(match) } }
                    .awaitAll()
            }
            
            todaysMatches.mapNotNull { it.leagueId }.distinct().forEach { leagueId ->
                leagueManager.updateStandings(leagueId)
            }
        }

        val isFirstOfMonth = calendar.get(Calendar.DAY_OF_MONTH) == 1
        val isCareerOpeningDay = calendar.get(Calendar.MONTH) == Calendar.AUGUST &&
            calendar.get(Calendar.DAY_OF_MONTH) == 1 && career.season.startsWith(calendar.get(Calendar.YEAR).toString())
        if (isFirstOfMonth && !isCareerOpeningDay) {
            managerRatingService.updateManagerRating(clubId)
            clubManager.updateAllClubsOverall()
            contractService.checkUnhappyContracts(clubId)
            checkContractExpiries(clubId, calendar.get(Calendar.YEAR))
            financialService.processMonthlyFinancials(clubId)
        }

        // Promotion and relegation are season-end operations, not daily work.
        if (calendar.get(Calendar.MONTH) == Calendar.JUNE && calendar.get(Calendar.DAY_OF_MONTH) == 1) {
            leagueManager.checkPromotionRelegation(club.leagueId)
        }

        val nextDate = advanceDay(currentDate)
        repository.saveCareer(career.copy(currentDate = nextDate, updatedAt = System.currentTimeMillis()))
        
        Log.d("GameDayProcessor", "Advanced to next day: $nextDate")
    }

    private suspend fun checkContractExpiries(clubId: Long, currentYear: Int) {
        val players = repository.getPlayersByClubSync(clubId)
        players.filter { it.contractUntil <= currentYear }.forEach { player ->
            sendInbox(
                clubId, 
                "Contract Expiry: ${player.shortName}", 
                "The contract of ${player.name} is set to expire this year. Consider offering an extension.", 
                InboxCategory.CONTRACT
            )
        }
    }

    private suspend fun handleTransferWindowNotifications(career: SaveCareerEntity, calendar: Calendar) {
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val clubId = career.selectedClubId
        
        if (month == Calendar.JULY && day == 1) {
            sendInbox(clubId, "Transfer Window", "The Summer Transfer Window is now OPEN.", InboxCategory.TRANSFER)
        } else if (month == Calendar.AUGUST && day == 31) {
            sendInbox(clubId, "Transfer Window", "The Summer Transfer Window is now CLOSED.", InboxCategory.TRANSFER)
        } else if (month == Calendar.JANUARY && day == 1) {
            sendInbox(clubId, "Transfer Window", "The Winter Transfer Window is now OPEN.", InboxCategory.TRANSFER)
        } else if (month == Calendar.JANUARY && day == 31) {
            sendInbox(clubId, "Transfer Window", "The Winter Transfer Window is now CLOSED.", InboxCategory.TRANSFER)
        }
    }

    private suspend fun sendInbox(clubId: Long, subject: String, message: String, category: InboxCategory) {
        val career = repository.getCareer().firstOrNull() ?: return
        repository.addInbox(
            InboxEntity(
                clubId = clubId,
                sender = "Club Management",
                subject = subject,
                message = message,
                category = category,
                timestamp = career.currentDate
            )
        )
    }

    private fun isTransferWindowOpen(date: Long): Boolean {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        val month = calendar.get(Calendar.MONTH)
        return month == Calendar.JULY || month == Calendar.AUGUST || month == Calendar.JANUARY
    }
    
    private fun isDeadlineDay(calendar: Calendar): Boolean {
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return (month == Calendar.AUGUST && day == 31) || (month == Calendar.JANUARY && day == 31)
    }

    private fun advanceDay(currentDate: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        return calendar.timeInMillis
    }
}
