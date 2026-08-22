package com.mountsa.fmsimulation.data.local.dao

import androidx.room.*
import com.mountsa.fmsimulation.data.local.entities.PlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {

    @Query("SELECT * FROM players ORDER BY overall DESC")
    fun getAllPlayers(): Flow<List<PlayerEntity>>

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )
    suspend fun insertAll(
        players: List<PlayerEntity>
    )

    @Update
    suspend fun updatePlayer(
        player: PlayerEntity
    )

    @Update
    suspend fun updatePlayers(
        players: List<PlayerEntity>
    )

    @Query(
        """
        SELECT * FROM players
        WHERE clubId = :clubId
        ORDER BY overall DESC
        """
    )
    fun getPlayersByClub(
        clubId: Long
    ): Flow<List<PlayerEntity>>

    @Query(
        """
        SELECT * FROM players
        WHERE clubId = :clubId
        ORDER BY overall DESC
        """
    )
    suspend fun getPlayersByClubSync(
        clubId: Long
    ): List<PlayerEntity>

    @Query(
        """
        SELECT * FROM players
        WHERE id = :playerId
        """
    )
    suspend fun getPlayerById(
        playerId: Long
    ): PlayerEntity?

    @Query(
        """
        SELECT * FROM players 
        ORDER BY goals DESC 
        LIMIT :limit
        """
    )
    fun getTopScorers(limit: Int = 20): Flow<List<PlayerEntity>>

    @Query(
        """
        SELECT * FROM players 
        ORDER BY assists DESC 
        LIMIT :limit
        """
    )
    fun getTopAssists(limit: Int = 20): Flow<List<PlayerEntity>>

    @Query(
        """
        SELECT * FROM players 
        ORDER BY averageRating DESC 
        LIMIT :limit
        """
    )
    fun getTopPlayers(limit: Int = 20): Flow<List<PlayerEntity>>

    @Query(
        """
        SELECT * FROM players 
        WHERE clubId IN (SELECT id FROM clubs WHERE leagueId = :leagueId)
        ORDER BY goals DESC 
        LIMIT :limit
        """
    )
    fun getTopScorersByLeague(leagueId: Long, limit: Int = 20): Flow<List<PlayerEntity>>

    @Query(
        """
        SELECT * FROM players 
        WHERE clubId IN (SELECT id FROM clubs WHERE leagueId = :leagueId)
        ORDER BY assists DESC 
        LIMIT :limit
        """
    )
    fun getTopAssistsByLeague(leagueId: Long, limit: Int = 20): Flow<List<PlayerEntity>>

    @Query(
        """
        SELECT * FROM players 
        WHERE clubId IN (SELECT id FROM clubs WHERE leagueId = :leagueId)
        ORDER BY averageRating DESC 
        LIMIT :limit
        """
    )
    fun getTopPlayersByLeague(leagueId: Long, limit: Int = 20): Flow<List<PlayerEntity>>
}
