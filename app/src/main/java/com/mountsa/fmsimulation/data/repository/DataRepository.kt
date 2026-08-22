package com.mountsa.fmsimulation.data.repository

import com.mountsa.fmsimulation.data.local.dao.*
import com.mountsa.fmsimulation.data.local.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataRepository @Inject constructor(
    private val nationDao: NationDao,
    private val leagueDao: LeagueDao,
    private val clubDao: ClubDao,
    private val playerDao: PlayerDao,
    private val userProfileDao: UserProfileDao,
    private val managerDao: ManagerDao,
    private val matchDao: MatchDao,
    private val standingDao: StandingDao,
    private val calendarDao: CalendarEventDao,
    private val inboxDao: InboxDao,
    private val objectiveDao: ObjectiveDao,
    private val transferDao: TransferOfferDao,
    private val saveCareerDao: SaveCareerDao,
    private val metadataDao: MetadataDao,
    private val seasonHistoryDao: SeasonHistoryDao,
    private val recordDao: RecordDao,
    private val scoutDao: ScoutDao
) {

    // ======================================
    // USER PROFILE
    // ======================================
    fun getAllProfiles(): Flow<List<UserProfileEntity>> = userProfileDao.getAllProfiles()
    suspend fun getProfileById(id: Long): UserProfileEntity? = userProfileDao.getProfileById(id)
    suspend fun insertProfile(profile: UserProfileEntity): Long = userProfileDao.insertProfile(profile)
    suspend fun deleteProfile(profile: UserProfileEntity) = userProfileDao.deleteProfile(profile)

    // ======================================
    // METADATA (Checkpoint System)
    // ======================================
    suspend fun saveMetadata(key: String, value: String) = metadataDao.insertMetadata(AppMetadataEntity(key, value))
    suspend fun getMetadata(key: String): String? = metadataDao.getMetadata(key)?.value

    // ======================================
    // NATION
    // ======================================

    fun getAllNations(): Flow<List<NationEntity>> = nationDao.getAllNations()
    suspend fun getAllNationsSync(): List<NationEntity> = nationDao.getAllNationsSync()
    suspend fun getNationById(id: Long): NationEntity? = nationDao.getNationById(id)
    suspend fun insertNations(nations: List<NationEntity>) { nationDao.insertAll(nations) }

    // ======================================
    // LEAGUE
    // ======================================

    fun getAllLeagues(): Flow<List<LeagueEntity>> = leagueDao.getAllLeagues()
    suspend fun getAllLeaguesSync(): List<LeagueEntity> = leagueDao.getAllLeaguesSync()
    fun getLeaguesByNation(nationId: Long): Flow<List<LeagueEntity>> = leagueDao.getLeaguesByNation(nationId)
    suspend fun getLeagueById(id: Long): LeagueEntity? = leagueDao.getLeagueById(id)
    suspend fun insertLeagues(leagues: List<LeagueEntity>) { leagueDao.insertAll(leagues) }

    // ======================================
    // CLUB
    // ======================================

    fun getAllClubs(): Flow<List<ClubEntity>> = clubDao.getAllClubs()
    suspend fun getAllClubsSync(): List<ClubEntity> = clubDao.getAllClubsSync()
    fun getClubsByLeague(leagueId: Long): Flow<List<ClubEntity>> = clubDao.getClubsByLeague(leagueId)
    suspend fun getClubById(clubId: Long): ClubEntity? = clubDao.getClubById(clubId)
    suspend fun insertClubs(clubs: List<ClubEntity>) { clubDao.insertAll(clubs) }
    suspend fun updateClub(club: ClubEntity) { clubDao.updateClub(club) }

    suspend fun getSelectedClub(): ClubEntity? {
        val career = getCareer().first() ?: return null
        return clubDao.getClubById(career.selectedClubId)
    }

    // ======================================
    // PLAYER
    // ======================================

    fun getAllPlayers(): Flow<List<PlayerEntity>> = playerDao.getAllPlayers()
    fun getPlayersByClub(clubId: Long): Flow<List<PlayerEntity>> = playerDao.getPlayersByClub(clubId)
    suspend fun getPlayersByClubSync(clubId: Long): List<PlayerEntity> = playerDao.getPlayersByClubSync(clubId)
    suspend fun getPlayerById(playerId: Long): PlayerEntity? = playerDao.getPlayerById(playerId)
    suspend fun updatePlayer(player: PlayerEntity) = playerDao.updatePlayer(player)
    suspend fun updatePlayers(players: List<PlayerEntity>) = playerDao.updatePlayers(players)
    suspend fun insertPlayers(players: List<PlayerEntity>) = playerDao.insertAll(players)

    fun getTopScorersByLeague(leagueId: Long, limit: Int = 20): Flow<List<PlayerEntity>> =
        playerDao.getTopScorersByLeague(leagueId, limit)
    fun getTopAssistsByLeague(leagueId: Long, limit: Int = 20): Flow<List<PlayerEntity>> =
        playerDao.getTopAssistsByLeague(leagueId, limit)
    fun getTopPlayersByLeague(leagueId: Long, limit: Int = 20): Flow<List<PlayerEntity>> =
        playerDao.getTopPlayersByLeague(leagueId, limit)

    // ======================================
    // MATCH
    // ======================================

    fun getUpcomingMatches(): Flow<List<MatchEntity>> = matchDao.getUpcomingMatches()
    fun getMatchesForClub(clubId: Long): Flow<List<MatchEntity>> = matchDao.getMatchesForClub(clubId)
    suspend fun insertMatches(matches: List<MatchEntity>) = matchDao.insertMatches(matches)
    suspend fun updateMatch(match: MatchEntity) = matchDao.updateMatch(match)
    suspend fun getMatchById(matchId: Long): MatchEntity? = matchDao.getMatchById(matchId)
    
    fun getNextMatchFlow(clubId: Long): Flow<MatchEntity?> = matchDao.getNextMatchFlow(clubId)
    suspend fun getNextMatch(clubId: Long): MatchEntity? = matchDao.getNextMatch(clubId)

    suspend fun getMatchesByDate(date: Long): List<MatchEntity> = matchDao.getMatchesForDate(date)

    suspend fun getTodayUserMatch(clubId: Long, date: Long): MatchEntity? {
        return matchDao.getMatchesForDate(date).firstOrNull {
            !it.isPlayed && (it.homeClubId == clubId || it.awayClubId == clubId)
        }
    }

    suspend fun deleteMatchesByLeague(leagueId: Long) = matchDao.deleteMatchesByLeague(leagueId)

    // ======================================
    // STANDINGS
    // ======================================

    fun getLeagueStandings(leagueId: Long): Flow<List<StandingEntity>> = standingDao.getLeagueStandings(leagueId)
    suspend fun insertStandings(standings: List<StandingEntity>) = standingDao.insertStandings(standings)
    suspend fun updateStanding(standing: StandingEntity) = standingDao.updateStanding(standing)
    suspend fun getStandingByClub(leagueId: Long, clubId: Long): StandingEntity? {
        val standings = standingDao.getLeagueStandings(leagueId).first()
        return standings.find { it.clubId == clubId }
    }

    suspend fun initializeLeagueStandings(leagueId: Long) {
        val clubs = getClubsByLeague(leagueId).first()
        val existingStandings = standingDao.getLeagueStandings(leagueId).first()
        if (existingStandings.isEmpty()) {
            val standings = clubs.mapIndexed { index, club ->
                StandingEntity(leagueId = leagueId, clubId = club.id, position = index + 1)
            }
            insertStandings(standings)
        }
    }

    // ======================================
    // OBJECTIVES
    // ======================================

    fun getObjectives(clubId: Long): Flow<List<ObjectiveEntity>> = objectiveDao.getObjectives(clubId)
    suspend fun insertObjectives(objectives: List<ObjectiveEntity>) = objectiveDao.insertObjectives(objectives)
    suspend fun clearObjectives(clubId: Long) = objectiveDao.clearObjectives(clubId)

    // ======================================
    // SCOUTING
    // ======================================
    fun getScoutsForClub(clubId: Long): Flow<List<ScoutEntity>> = scoutDao.getScoutsByClub(clubId)
    suspend fun insertScout(scout: ScoutEntity) = scoutDao.insertScout(scout)
    suspend fun updateScout(scout: ScoutEntity) = scoutDao.updateScout(scout)
    fun getAssignmentsForScout(scoutId: Long): Flow<List<ScoutAssignmentEntity>> = scoutDao.getAssignmentsForScout(scoutId)
    suspend fun insertAssignment(assignment: ScoutAssignmentEntity) = scoutDao.insertAssignment(assignment)
    suspend fun deleteAssignment(assignmentId: Long) = scoutDao.deleteAssignment(assignmentId)

    // ======================================
    // OTHERS
    // ======================================

    fun getCalendar(clubId: Long): Flow<List<CalendarEventEntity>> = calendarDao.getCalendar(clubId)
    suspend fun deleteEventsByType(clubId: Long, type: String) = calendarDao.deleteEventsByType(clubId, type)
    suspend fun insertEvents(events: List<CalendarEventEntity>) = calendarDao.insertEvents(events)

    fun getInbox(clubId: Long): Flow<List<InboxEntity>> = inboxDao.getInbox(clubId)
    fun getUnreadInboxCount(clubId: Long): Flow<Int> = inboxDao.unreadCount(clubId)
    suspend fun addInbox(inbox: InboxEntity) = inboxDao.insertInbox(inbox)
    suspend fun markInboxAsRead(inboxId: Long) = inboxDao.markAsRead(inboxId)

    fun getOffersForClub(clubId: Long): Flow<List<TransferOfferEntity>> = transferDao.getOffersForClub(clubId)
    suspend fun insertOffer(offer: TransferOfferEntity): Long = transferDao.insertOffer(offer)
    suspend fun updateOffer(offer: TransferOfferEntity) = transferDao.updateOffer(offer)

    suspend fun saveCareer(career: SaveCareerEntity) = saveCareerDao.saveCareer(career)

    /**
     * Wipes all progress from the current save so a brand-new career can be
     * started cleanly: the career record itself, all played/scheduled
     * matches, standings, inbox messages, and open transfer offers. The
     * shared football world data (nations/leagues/clubs/players) is left
     * intact and will be re-used/reseeded by DatabaseSeeder as needed.
     */
    suspend fun resetCareerData() {
        saveCareerDao.deleteCareer()
        matchDao.deleteAllMatches()
        standingDao.deleteAllStandings()
        inboxDao.deleteAllInbox()
        transferDao.deleteAllOffers()
        metadataDao.deleteMetadata("CAREER_FLOW_STEP")
    }
    fun getCareer(): Flow<SaveCareerEntity?> = saveCareerDao.getCareer()

    suspend fun getClubName(clubId: Long): String = clubDao.getClubById(clubId)?.name ?: "Unknown Club"
    suspend fun getLeagueName(leagueId: Long): String = leagueDao.getLeagueById(leagueId)?.name ?: "Unknown League"

    // ======================================
    // SEASON HISTORY
    // ======================================
    suspend fun insertSeasonHistory(history: SeasonHistoryEntity) = seasonHistoryDao.insertHistory(history)
    fun getSeasonHistory(clubId: Long): Flow<List<SeasonHistoryEntity>> = seasonHistoryDao.getHistoryByClub(clubId)

    // ======================================
    // RECORDS
    // ======================================
    suspend fun insertRecord(record: RecordEntity) = recordDao.insertRecord(record)
    suspend fun getRecordByKey(key: String, clubId: Long): RecordEntity? = recordDao.getRecordByKey(key, clubId)
    fun getRecordsByClub(clubId: Long): Flow<List<RecordEntity>> = recordDao.getRecordsByClub(clubId)
}
