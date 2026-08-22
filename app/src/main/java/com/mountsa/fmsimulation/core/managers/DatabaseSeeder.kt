package com.mountsa.fmsimulation.core.managers

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.mountsa.fmsimulation.data.local.dao.MetadataDao
import com.mountsa.fmsimulation.data.local.entities.AppMetadataEntity
import com.mountsa.fmsimulation.data.local.entities.ClubEntity
import com.mountsa.fmsimulation.data.local.entities.LeagueEntity
import com.mountsa.fmsimulation.data.local.entities.NationEntity
import com.mountsa.fmsimulation.data.local.entities.PlayerEntity
import com.mountsa.fmsimulation.data.repository.DataRepository
import com.mountsa.fmsimulation.domain.services.FixtureGenerator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class DatabaseSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: DataRepository,
    private val fixtureGenerator: FixtureGenerator,
    private val metadataDao: MetadataDao
) {
    private val gson = Gson()
    private val CURRENT_DB_VERSION = "12.0"

    private val _loadingMessage = MutableStateFlow("Preparing database...")
    val loadingMessage = _loadingMessage.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress = _progress.asStateFlow()

    private var clubToLeagueMap = mapOf<Long, Long>()
    private var clubPlayerCountMap = mapOf<Long, Int>()
    private var availableFaces = setOf<String>()

    private val leagueToNationMap = mapOf(
        1L to 13L, 4L to 7L, 10L to 34L, 13L to 14L, 14L to 14L, 16L to 18L,
        17L to 18L, 19L to 21L, 20L to 21L, 31L to 27L, 32L to 27L, 39L to 95L,
        41L to 36L, 50L to 42L, 53L to 45L, 54L to 45L, 56L to 46L, 60L to 14L,
        61L to 14L, 65L to 25L, 66L to 37L, 68L to 48L, 80L to 4L, 83L to 167L,
        308L to 38L, 330L to 39L, 351L to 195L, 2012L to 22L, 2076L to 21L,
        2149L to 155L, 2216L to 14L
    )

    suspend fun seedIfNeeded() = withContext(Dispatchers.IO) {
        _progress.value = 0.05f
        delay(200)
        _progress.value = 0.10f
        _loadingMessage.value = "Checking database..."
        delay(400)
        
        try {
            val dbVersionMeta = metadataDao.getMetadata("DB_VERSION")
            if (dbVersionMeta == null || dbVersionMeta.value != CURRENT_DB_VERSION) {
                seedAll()
                metadataDao.insertMetadata(AppMetadataEntity("DB_VERSION", CURRENT_DB_VERSION))
            } else {
                refreshClubRatingsFromSquads()
                _loadingMessage.value = "Loading assets..."
                delay(600) 
                _progress.value = 0.45f
                delay(500)
                _progress.value = 0.85f
                _loadingMessage.value = "Checking career status..."
                delay(500)
                _progress.value = 1f
                _loadingMessage.value = "Ready!"
            }
        } catch (e: Exception) {
            Log.e("DatabaseSeeder", "Seed Failed", e)
        }
    }

    private suspend fun seedAll() {
        _progress.value = 0.15f
        _loadingMessage.value = "Analyzing Data..."
        loadDynamicMappingsStream()
        loadAvailableFaces()
        _progress.value = 0.20f
        delay(200)

        _loadingMessage.value = "Seeding Nations..."
        seedNations()
        _progress.value = 0.30f
        delay(200)

        _loadingMessage.value = "Seeding Leagues..."
        seedLeagues()
        _progress.value = 0.40f
        delay(200)

        _loadingMessage.value = "Seeding Clubs..."
        seedClubs()
        _progress.value = 0.50f
        delay(200)

        _loadingMessage.value = "Seeding Players..."
        seedPlayers()
        refreshClubRatingsFromSquads()

        _loadingMessage.value = "Generating Fixtures..."
        seedFixtures()
        _progress.value = 0.95f
        
        delay(500)
        _progress.value = 1f
        _loadingMessage.value = "Ready!"
    }

    private fun loadAvailableFaces() {
        try {
            availableFaces = context.assets.list("database/faces")?.toSet() ?: emptySet()
        } catch (e: Exception) {
            availableFaces = emptySet()
        }
    }

    private fun loadDynamicMappingsStream() {
        val tempClubToLeague = mutableMapOf<Long, Long>()
        val tempClubPlayerCount = mutableMapOf<Long, Int>()
        try {
            context.assets.open("database/database_players.json").use { inputStream ->
                val reader = JsonReader(InputStreamReader(inputStream))
                reader.beginObject()
                while (reader.hasNext()) {
                    if (reader.nextName() == "players") {
                        reader.beginArray()
                        while (reader.hasNext()) {
                            reader.beginObject()
                            var clubId = 0L
                            var leagueId = 0L
                            while (reader.hasNext()) {
                                when (reader.nextName()) {
                                    "club_team_id" -> clubId = reader.nextLong()
                                    "league_id" -> leagueId = reader.nextLong()
                                    else -> reader.skipValue()
                                }
                            }
                            if (clubId != 0L) {
                                if (leagueId != 0L) tempClubToLeague[clubId] = leagueId
                                tempClubPlayerCount[clubId] = (tempClubPlayerCount[clubId] ?: 0) + 1
                            }
                            reader.endObject()
                        }
                        reader.endArray()
                    } else reader.skipValue()
                }
                reader.endObject()
            }
        } catch (e: Exception) {}
        clubToLeagueMap = tempClubToLeague
        clubPlayerCountMap = tempClubPlayerCount
    }

    private suspend fun seedNations() {
        try {
            context.assets.open("database/nationality.json").use { isr ->
                val type = object : TypeToken<Map<String, Any>>() {}.type
                val data: Map<String, Any> = gson.fromJson(InputStreamReader(isr), type)
                
                @Suppress("UNCHECKED_CAST")
                val nationalities = data["nationalities"] as? List<Map<String, Any>> ?: emptyList()

                val nations = nationalities.map {
                    val id = (it["nationality_id"] as Number).toLong()
                    val name = it["nationality_name"] as String
                    
                    val flagFile = when (name) {
                        "Australia" -> "AU"
                        "Austria" -> "AT"
                        "England" -> "ENGLAND"
                        "Scotland" -> "SCOTLAND"
                        "Wales" -> "WALES"
                        "China PR" -> "CN"
                        else -> name.take(2).uppercase()
                    }

                    NationEntity(
                        id = id, 
                        name = name, 
                        shortName = if (name == "Austria") "AUT" else name.take(3).uppercase(), 
                        flagAsset = "database/logo/flag/$flagFile.webp"
                    )
                }
                repository.insertNations(nations)
            }
        } catch (e: Exception) {}
    }

    private suspend fun seedLeagues() {
        try {
            context.assets.open("database/leagues.json").use { isr ->
                val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                val leaguesData: List<Map<String, Any>> = gson.fromJson(InputStreamReader(isr), type)

                val leagues = leaguesData.mapNotNull {
                    val leagueId = (it["league_id"] as Number).toLong()
                    val name = it["league_name"] as String
                    val nationId = leagueToNationMap[leagueId] ?: return@mapNotNull null

                    LeagueEntity(
                        id = leagueId, nationId = nationId, name = name,
                        shortName = name.split(" ").mapNotNull { s -> s.firstOrNull() }.joinToString("").uppercase(),
                        logoAsset = "database/logo/league/l$leagueId.webp", division = 1, reputation = 70
                    )
                }
                repository.insertLeagues(leagues)
            }
        } catch (e: Exception) {}
    }

    private suspend fun seedClubs() {
        try {
            context.assets.open("database/teams.json").use { isr ->
                val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                val teamsData: List<Map<String, Any>> = gson.fromJson(InputStreamReader(isr), type)

                val clubs = teamsData.mapNotNull { team ->
                    val teamId = (team["club_team_id"] as Number).toLong()
                    val leagueId = clubToLeagueMap[teamId] ?: return@mapNotNull null
                    val name = team["club_name"] as String
                    val nationId = leagueToNationMap[leagueId] ?: 14L
                    
                    ClubEntity(
                        id = teamId, nationId = nationId, leagueId = leagueId, name = name,
                        shortName = name.take(3).uppercase(), logoAsset = "database/logo/club/l$teamId.webp",
                        stadium = "Stadium", budget = 100_000_000L, overall = 75, attack = 75, midfield = 75, defense = 75,
                        reputation = 70, playerCount = clubPlayerCountMap[teamId] ?: 0
                    )
                }
                repository.insertClubs(clubs)
            }
        } catch (e: Exception) {}
    }

    private suspend fun seedPlayers() {
        val nationFlagMap = repository.getAllNationsSync().associate { it.id to it.flagAsset }
        
        try {
            context.assets.open("database/database_players.json").use { inputStream ->
                val reader = JsonReader(InputStreamReader(inputStream))
                val playerBatch = mutableListOf<PlayerEntity>()
                reader.beginObject()
                while (reader.hasNext()) {
                    if (reader.nextName() == "players") {
                        reader.beginArray()
                        var count = 0
                        while (reader.hasNext()) {
                            parsePlayer(reader, nationFlagMap)?.let { playerBatch.add(it) }
                            count++
                            if (playerBatch.size >= 500) {
                                repository.insertPlayers(playerBatch.toList())
                                playerBatch.clear()
                                _progress.value = 0.55f + (minOf(count.toFloat() / 20000f, 1f) * 0.35f)
                            }
                        }
                        reader.endArray()
                    } else reader.skipValue()
                }
                reader.endObject()
                if (playerBatch.isNotEmpty()) repository.insertPlayers(playerBatch)
            }
        } catch (e: Exception) {
            Log.e("DatabaseSeeder", "Seed Players Failed: ${e.message}")
        }
    }

    /**
     * Rebuild club strength from the actual database squads. Older installs used
     * the same hard-coded 75 for every club, which made career selection and the
     * match engine ignore the supplied player database. This updates only rating
     * fields, preserving budgets, tactics and career progress.
     */
    private suspend fun refreshClubRatingsFromSquads() {
        val clubs = repository.getAllClubsSync()
        clubs.forEach { club ->
            val squad = repository.getPlayersByClubSync(club.id)
            if (squad.isEmpty()) return@forEach

            fun lineRating(positions: Set<String>, take: Int): Int {
                val candidates = squad.filter { player ->
                    val roles = (listOf(player.position) + player.secondaryPosition.split(','))
                        .map { it.trim().uppercase() }
                    roles.any { it in positions }
                }.sortedByDescending { it.overall }.take(take)
                return candidates.takeIf { it.isNotEmpty() }
                    ?.map { it.overall }
                    ?.average()
                    ?.toInt()
                    ?: squad.sortedByDescending { it.overall }.take(take).map { it.overall }.average().toInt()
            }

            val attack = lineRating(setOf("ST", "CF", "LW", "RW", "LF", "RF"), 5)
            val midfield = lineRating(setOf("CM", "CAM", "CDM", "LM", "RM", "AM"), 6)
            val defense = lineRating(setOf("GK", "CB", "LB", "RB", "LWB", "RWB"), 7)
            val overall = ((attack * .34f) + (midfield * .33f) + (defense * .33f)).toInt()
            if (club.attack != attack || club.midfield != midfield || club.defense != defense || club.overall != overall) {
                repository.updateClub(
                    club.copy(
                        attack = attack.coerceIn(40, 99),
                        midfield = midfield.coerceIn(40, 99),
                        defense = defense.coerceIn(40, 99),
                        overall = overall.coerceIn(40, 99)
                    )
                )
            }
        }
    }

    private fun parsePlayer(reader: JsonReader, nationFlagMap: Map<Long, String>): PlayerEntity? {
        try {
            reader.beginObject()
            var id = 0L; var clubId = 0L; var nationId = 0L; var name = ""; var shortName = ""
            var shirtNumber = 0; var age = 20; var overall = 0; var potential = 0
            var contractUntil = 2025; var valueEur = 0L; var wageEur = 0L
            var positions = listOf<String>()
            
            var pace = 50; var shooting = 50; var passing = 50; var dribbling = 50; var defending = 50; var physical = 50
            var vision = 50; var positioning = 50; var aggression = 50; var interceptions = 50; var penalties = 50; var composure = 50
            var stamina = 50; var jumping = 50; var strength = 50; var shotPower = 50; var longShots = 50
            var skillDribbling = 50; var curve = 50; var fkAccuracy = 50; var ballControl = 50; var longPassing = 50
            var standingTackle = 50; var slidingTackle = 50; var markingAwareness = 50
            var agility = 50; var sprintSpeed = 50; var reactions = 50; var acceleration = 50; var balance = 50
            var finishing = 50; var volleys = 50; var crossing = 50; var headingAccuracy = 50; var shortPassing = 50
            var gkDiving = 10; var gkReflexes = 10; var gkHandling = 10; var gkPositioning = 10; var gkKicking = 10

            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "player_id" -> id = reader.nextLong()
                    "club_team_id" -> clubId = reader.nextLong()
                    "nationality_id" -> nationId = reader.nextLong()
                    "long_name" -> name = reader.nextString()
                    "short_name" -> shortName = reader.nextString()
                    "age" -> age = reader.nextInt()
                    "club_jersey_number" -> shirtNumber = reader.nextInt()
                    "overall" -> overall = reader.nextInt()
                    "potential" -> potential = reader.nextInt()
                    "value_eur" -> valueEur = reader.nextLong()
                    "wage_eur" -> wageEur = reader.nextLong()
                    "club_contract_valid_until_year" -> contractUntil = reader.nextInt()
                    "positions" -> {
                        reader.beginArray()
                        val posList = mutableListOf<String>()
                        while (reader.hasNext()) posList.add(reader.nextString())
                        positions = posList
                        reader.endArray()
                    }
                    "stats" -> {
                        reader.beginObject()
                        while (reader.hasNext()) {
                            when (reader.nextName()) {
                                "main" -> {
                                    reader.beginObject()
                                    while (reader.hasNext()) {
                                        when (reader.nextName()) {
                                            "pace" -> pace = reader.nextInt()
                                            "shooting" -> shooting = reader.nextInt()
                                            "passing" -> passing = reader.nextInt()
                                            "dribbling" -> dribbling = reader.nextInt()
                                            "defending" -> defending = reader.nextInt()
                                            "physic" -> physical = reader.nextInt()
                                            else -> reader.skipValue()
                                        }
                                    }
                                    reader.endObject()
                                }
                                "mentality" -> {
                                    reader.beginObject()
                                    while (reader.hasNext()) {
                                        when (reader.nextName()) {
                                            "vision" -> vision = reader.nextInt()
                                            "positioning" -> positioning = reader.nextInt()
                                            "aggression" -> aggression = reader.nextInt()
                                            "interceptions" -> interceptions = reader.nextInt()
                                            "penalties" -> penalties = reader.nextInt()
                                            "composure" -> composure = reader.nextInt()
                                            else -> reader.skipValue()
                                        }
                                    }
                                    reader.endObject()
                                }
                                "power" -> {
                                    reader.beginObject()
                                    while (reader.hasNext()) {
                                        when (reader.nextName()) {
                                            "stamina" -> stamina = reader.nextInt()
                                            "jumping" -> jumping = reader.nextInt()
                                            "strength" -> strength = reader.nextInt()
                                            "shot_power" -> shotPower = reader.nextInt()
                                            "long_shots" -> longShots = reader.nextInt()
                                            else -> reader.skipValue()
                                        }
                                    }
                                    reader.endObject()
                                }
                                "skill" -> {
                                    reader.beginObject()
                                    while (reader.hasNext()) {
                                        when (reader.nextName()) {
                                            "dribbling" -> skillDribbling = reader.nextInt()
                                            "curve" -> curve = reader.nextInt()
                                            "fk_accuracy" -> fkAccuracy = reader.nextInt()
                                            "ball_control" -> ballControl = reader.nextInt()
                                            "long_passing" -> longPassing = reader.nextInt()
                                            else -> reader.skipValue()
                                        }
                                    }
                                    reader.endObject()
                                }
                                "defending" -> {
                                    reader.beginObject()
                                    while (reader.hasNext()) {
                                        when (reader.nextName()) {
                                            "standing_tackle" -> standingTackle = reader.nextInt()
                                            "sliding_tackle" -> slidingTackle = reader.nextInt()
                                            "marking_awareness" -> markingAwareness = reader.nextInt()
                                            else -> reader.skipValue()
                                        }
                                    }
                                    reader.endObject()
                                }
                                "movement" -> {
                                    reader.beginObject()
                                    while (reader.hasNext()) {
                                        when (reader.nextName()) {
                                            "agility" -> agility = reader.nextInt()
                                            "sprint_speed" -> sprintSpeed = reader.nextInt()
                                            "reactions" -> reactions = reader.nextInt()
                                            "acceleration" -> acceleration = reader.nextInt()
                                            "balance" -> balance = reader.nextInt()
                                            else -> reader.skipValue()
                                        }
                                    }
                                    reader.endObject()
                                }
                                "attacking" -> {
                                    reader.beginObject()
                                    while (reader.hasNext()) {
                                        when (reader.nextName()) {
                                            "finishing" -> finishing = reader.nextInt()
                                            "volleys" -> volleys = reader.nextInt()
                                            "crossing" -> crossing = reader.nextInt()
                                            "heading_accuracy" -> headingAccuracy = reader.nextInt()
                                            "short_passing" -> shortPassing = reader.nextInt()
                                            else -> reader.skipValue()
                                        }
                                    }
                                    reader.endObject()
                                }
                                "goalkeeping" -> {
                                    reader.beginObject()
                                    while (reader.hasNext()) {
                                        when (reader.nextName()) {
                                            "diving" -> gkDiving = reader.nextInt()
                                            "reflexes" -> gkReflexes = reader.nextInt()
                                            "handling" -> gkHandling = reader.nextInt()
                                            "positioning" -> gkPositioning = reader.nextInt()
                                            "kicking" -> gkKicking = reader.nextInt()
                                            else -> reader.skipValue()
                                        }
                                    }
                                    reader.endObject()
                                }
                                else -> reader.skipValue()
                            }
                        }
                        reader.endObject()
                    }
                    else -> reader.skipValue()
                }
            }
            reader.endObject()

            val faceFile = "$id.webp"
            val avatarPath = if (availableFaces.contains(faceFile)) {
                "database/faces/$faceFile"
            } else {
                "database/faces/0.webp"
            }

            return PlayerEntity(
                id = id, clubId = clubId, nationId = nationId, name = name, shortName = shortName,
                shirtNumber = shirtNumber, age = age, position = positions.firstOrNull() ?: "CM", 
                secondaryPosition = if (positions.size > 1) positions.drop(1).joinToString(",") else "",
                overall = overall, potential = potential,
                pace = pace, shooting = shooting, passing = passing, dribbling = dribbling, defending = defending, physical = physical,
                vision = vision, positioning = positioning, aggression = aggression, interceptions = interceptions, penalties = penalties, composure = composure,
                stamina = stamina, jumping = jumping, strength = strength, shotPower = shotPower, longShots = longShots,
                skillDribbling = skillDribbling, curve = curve, fkAccuracy = fkAccuracy, ballControl = ballControl, longPassing = longPassing,
                standingTackle = standingTackle, slidingTackle = slidingTackle, markingAwareness = markingAwareness,
                agility = agility, sprintSpeed = sprintSpeed, reactions = reactions, acceleration = acceleration, balance = balance,
                finishing = finishing, volleys = volleys, crossing = crossing, headingAccuracy = headingAccuracy, shortPassing = shortPassing,
                gkDiving = gkDiving, gkReflexes = gkReflexes, gkHandling = gkHandling, gkPositioning = gkPositioning, gkKicking = gkKicking,
                marketValue = valueEur, wage = wageEur,
                contractUntil = contractUntil, avatarAsset = avatarPath,
                flagAsset = nationFlagMap[nationId] ?: "database/logo/flag/UN.webp",
                form = 50 + Random.nextInt(-10, 25)
            )
        } catch (e: Exception) { 
            return null 
        }
    }

    /**
     * Regenerates match fixtures and initializes standings for every league.
     * Safe to call again later (e.g. after resetCareerData()) to repopulate
     * the schedule when it was wiped for a fresh new career.
     */
    suspend fun seedFixtures() {
        val leagues = repository.getAllLeaguesSync()
        leagues.forEach { 
            fixtureGenerator.generateLeagueFixtures(it.id)
            repository.initializeLeagueStandings(it.id)
        }
    }
}
