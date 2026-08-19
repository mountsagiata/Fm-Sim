package com.mountsa.fmsimulation.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mountsa.fmsimulation.data.local.dao.*
import com.mountsa.fmsimulation.data.local.entities.*

@Database(
    entities = [
        NationEntity::class,
        LeagueEntity::class,
        ClubEntity::class,
        PlayerEntity::class,
        ManagerEntity::class,
        MatchEntity::class,
        StandingEntity::class,
        CalendarEventEntity::class,
        InboxEntity::class,
        ObjectiveEntity::class,
        TransferOfferEntity::class,
        SaveCareerEntity::class,
        AppMetadataEntity::class,
        UserProfileEntity::class,
        SeasonHistoryEntity::class,
        RecordEntity::class,
        ScoutEntity::class,
        ScoutAssignmentEntity::class
    ],
    version = 15,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun nationDao(): NationDao
    abstract fun leagueDao(): LeagueDao
    abstract fun clubDao(): ClubDao
    abstract fun playerDao(): PlayerDao
    abstract fun managerDao(): ManagerDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun matchDao(): MatchDao
    abstract fun standingDao(): StandingDao
    abstract fun calendarEventDao(): CalendarEventDao
    abstract fun inboxDao(): InboxDao
    abstract fun objectiveDao(): ObjectiveDao
    abstract fun transferOfferDao(): TransferOfferDao
    abstract fun saveCareerDao(): SaveCareerDao
    abstract fun metadataDao(): MetadataDao
    abstract fun seasonHistoryDao(): SeasonHistoryDao
    abstract fun recordDao(): RecordDao
    abstract fun scoutDao(): ScoutDao
}
