package com.mountsa.fmsimulation.domain.services

import com.mountsa.fmsimulation.core.enums.InboxCategory
import com.mountsa.fmsimulation.data.local.entities.ClubEntity
import com.mountsa.fmsimulation.data.local.entities.InboxEntity
import com.mountsa.fmsimulation.data.local.entities.MatchEntity
import com.mountsa.fmsimulation.data.repository.DataRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FinancialService @Inject constructor(
    private val repository: DataRepository
) {
    /**
     * Item 26 & 27: Calculate match day income based on attendance and ticket prices.
     */
    suspend fun processMatchDayFinancials(match: MatchEntity) {
        val homeClub = repository.getClubById(match.homeClubId) ?: return
        
        // 1. Calculate Attendance
        val baseAttendance = (homeClub.reputation * 500) + Random.nextInt(-1000, 1000)
        // Placeholder capacity: assume reputation * 1000 is capacity
        val capacity = homeClub.reputation * 1000 
        val attendance = baseAttendance.coerceIn(capacity / 2, capacity)
        
        // 2. Ticket Income
        val avgTicketPrice = 35 // Base price
        val ticketIncome = (attendance * avgTicketPrice).toLong()
        
        // 3. Match Day Expenses
        val expenses = (attendance * 5).toLong()
        val netMatchIncome = ticketIncome - expenses

        repository.updateClub(homeClub.copy(budget = homeClub.budget + netMatchIncome))
        
        // Notify if it's the user's club
        val career = repository.getCareer().first()
        if (career?.selectedClubId == homeClub.id) {
            sendFinancialInbox(
                homeClub.id,
                "Match Day Income",
                "Gate receipts from today's match against ${repository.getClubById(match.awayClubId)?.name}: €${netMatchIncome}. Total attendance: $attendance.",
                career.currentDate
            )
        }
    }

    /**
     * Periodic wage deduction and sponsorship income.
     */
    suspend fun processMonthlyFinancials(clubId: Long) {
        val club = repository.getClubById(clubId) ?: return
        val players = repository.getPlayersByClubSync(clubId)
        val career = repository.getCareer().first() ?: return

        // 1. Wage Expenses
        val totalMonthlyWages = players.sumOf { it.wage } * 4 // 4 weeks
        
        // 2. Sponsorship Income (Based on reputation)
        val sponsorIncome = (club.reputation * 25_000L) + Random.nextLong(100_000, 500_000)
        
        val netMonthly = sponsorIncome - totalMonthlyWages
        repository.updateClub(club.copy(budget = club.budget + netMonthly))

        sendFinancialInbox(
            clubId,
            "Monthly Financial Report",
            "Sponsorship income: €${sponsorIncome}. Player wages: €${totalMonthlyWages}. Monthly balance: €${netMonthly}.",
            career.currentDate
        )
    }

    private suspend fun sendFinancialInbox(clubId: Long, subject: String, message: String, date: Long) {
        repository.addInbox(
            InboxEntity(
                clubId = clubId,
                sender = "Finance Department",
                subject = subject,
                message = message,
                category = InboxCategory.BOARD,
                timestamp = date
            )
        )
    }
}
