package com.mountsa.fmsimulation.di

import android.content.Context
import androidx.room.Room
import com.mountsa.fmsimulation.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "fm_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideNationDao(db: AppDatabase) = db.nationDao()

    @Provides
    fun provideLeagueDao(db: AppDatabase) = db.leagueDao()

    @Provides
    fun provideClubDao(db: AppDatabase) = db.clubDao()

    @Provides
    fun providePlayerDao(db: AppDatabase) = db.playerDao()

    @Provides
    fun provideManagerDao(db: AppDatabase) = db.managerDao()

    @Provides
    fun provideUserProfileDao(db: AppDatabase) = db.userProfileDao()

    @Provides
    fun provideMatchDao(db: AppDatabase) = db.matchDao()

    @Provides
    fun provideStandingDao(db: AppDatabase) = db.standingDao()

    @Provides
    fun provideCalendarDao(db: AppDatabase) = db.calendarEventDao()

    @Provides
    fun provideInboxDao(db: AppDatabase) = db.inboxDao()

    @Provides
    fun provideObjectiveDao(db: AppDatabase) = db.objectiveDao()

    @Provides
    fun provideTransferDao(db: AppDatabase) = db.transferOfferDao()

    @Provides
    fun provideSaveCareerDao(db: AppDatabase) = db.saveCareerDao()

    @Provides
    fun provideMetadataDao(db: AppDatabase) = db.metadataDao()

    @Provides
    fun provideSeasonHistoryDao(db: AppDatabase) = db.seasonHistoryDao()

    @Provides
    fun provideRecordDao(db: AppDatabase) = db.recordDao()

    @Provides
    fun provideScoutDao(db: AppDatabase) = db.scoutDao()
}
