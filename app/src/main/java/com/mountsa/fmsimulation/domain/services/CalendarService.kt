package com.mountsa.fmsimulation.domain.services

import com.mountsa.fmsimulation.data.local.entities.CalendarEventEntity
import com.mountsa.fmsimulation.data.local.entities.MatchEntity
import com.mountsa.fmsimulation.data.repository.DataRepository
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarService @Inject constructor(
    private val repository: DataRepository
) {
    /**
     * Item 20: Automates calendar generation for training, recovery, press, etc.
     */
    suspend fun automateCalendar(clubId: Long, currentDate: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentDate
        
        val matches = repository.getMatchesByDate(currentDate)
        val isMatchDay = matches.any { it.homeClubId == clubId || it.awayClubId == clubId }

        val events = mutableListOf<CalendarEventEntity>()

        if (isMatchDay) {
            events.add(CalendarEventEntity(clubId = clubId, eventDate = currentDate, type = "MATCH", title = "Matchday", description = "Focus on the upcoming match."))
            events.add(CalendarEventEntity(clubId = clubId, eventDate = currentDate, type = "MEDIA", title = "Press Conference", description = "Pre and post match media duties."))
        } else {
            // Check previous day for match to schedule recovery
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            val yesterdayMatches = repository.getMatchesByDate(calendar.timeInMillis)
            val wasMatchYesterday = yesterdayMatches.any { it.homeClubId == clubId || it.awayClubId == clubId }
            
            if (wasMatchYesterday) {
                events.add(CalendarEventEntity(clubId = clubId, eventDate = currentDate, type = "RECOVERY", title = "Recovery Session", description = "Light session to improve fitness."))
            } else {
                events.add(CalendarEventEntity(clubId = clubId, eventDate = currentDate, type = "TRAINING", title = "Standard Training", description = "Regular tactical and physical drills."))
            }
        }

        // Special Events
        calendar.timeInMillis = currentDate
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)

        if (day == 1) {
            events.add(CalendarEventEntity(clubId = clubId, eventDate = currentDate, type = "BOARD", title = "Board Meeting", description = "Monthly progress review with the board."))
        }

        if ((month == Calendar.AUGUST || month == Calendar.JANUARY) && day == 31) {
            events.add(CalendarEventEntity(clubId = clubId, eventDate = currentDate, type = "TRANSFER", title = "Deadline Day", description = "Last chance to finalize deals!"))
        }

        if (month == Calendar.MARCH && day == 15) {
            events.add(CalendarEventEntity(clubId = clubId, eventDate = currentDate, type = "YOUTH", title = "Youth Intake Day", description = "New prospects arriving at the academy."))
        }

        repository.insertEvents(events)
    }
}
