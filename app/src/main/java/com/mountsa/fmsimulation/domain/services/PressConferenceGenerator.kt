package com.mountsa.fmsimulation.domain.services

import com.google.gson.Gson
import com.mountsa.fmsimulation.core.enums.InboxCategory
import com.mountsa.fmsimulation.data.local.entities.InboxEntity
import com.mountsa.fmsimulation.data.repository.DataRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

enum class PressType { PRE_MATCH, POST_MATCH, TRANSFER, DERBY }

data class PressOption(
    val id: String,
    val text: String,
    val moraleImpact: Int,
    val ratingImpact: Int,
    val style: String // AGGRESSIVE, CALM, POSITIVE, EVASIVE
)

data class PressQuestion(
    val text: String,
    val options: List<PressOption>
)

@Singleton
class PressConferenceGenerator @Inject constructor(
    private val repository: DataRepository
) {
    private val gson = Gson()

    private val preMatchQuestions = listOf(
        PressQuestion(
            "How do you feel about the team's recent form?",
            listOf(
                PressOption("1", "I'm very confident in our current momentum.", 5, 2, "POSITIVE"),
                PressOption("2", "We need to stay grounded and keep working.", 2, 1, "CALM"),
                PressOption("3", "The form is irrelevant if we don't perform today.", -2, 0, "AGGRESSIVE"),
                PressOption("4", "No comment on that.", -1, -1, "EVASIVE")
            )
        ),
        PressQuestion(
            "What is your assessment of the opponent today?",
            listOf(
                PressOption("1", "They are a strong team, but we are better.", 3, 1, "POSITIVE"),
                PressOption("2", "We have identified their weaknesses.", 2, 2, "CALM"),
                PressOption("3", "They don't stand a chance.", 5, -1, "AGGRESSIVE"),
                PressOption("4", "We treat every opponent with respect.", 1, 1, "CALM")
            )
        )
    )

    private val postMatchQuestions = listOf(
        PressQuestion(
            "Are you satisfied with the result today?",
            listOf(
                PressOption("1", "The boys gave everything, I couldn't be happier.", 6, 2, "POSITIVE"),
                PressOption("2", "It's a fair result based on the play.", 1, 1, "CALM"),
                PressOption("3", "The refereeing was an absolute disgrace.", -4, -3, "AGGRESSIVE"),
                PressOption("4", "We should have done much better than this.", -2, 1, "CALM")
            )
        )
    )

    suspend fun generatePressConference(clubId: Long, type: PressType = PressType.PRE_MATCH) {
        val threshold = if (type == PressType.TRANSFER) 0.15f else 0.5f
        if (Random.nextFloat() > threshold) return 

        val career = repository.getCareer().first() ?: return
        
        val question = when(type) {
            PressType.PRE_MATCH -> preMatchQuestions.random()
            PressType.POST_MATCH -> postMatchQuestions.random()
            else -> preMatchQuestions.random() 
        }

        val subject = when(type) {
            PressType.PRE_MATCH -> "Pre-Match Press Conference"
            PressType.POST_MATCH -> "Post-Match Interview"
            PressType.TRANSFER -> "Media Transfer Speculation"
            PressType.DERBY -> "Derby Day Media Scrutiny"
        }

        repository.addInbox(
            InboxEntity(
                clubId = clubId,
                sender = "Media Liaison",
                subject = subject,
                message = "The media is waiting. They want to know: '${question.text}'.",
                category = InboxCategory.MEDIA,
                actionData = gson.toJson(question.options),
                timestamp = career.currentDate
            )
        )
    }
}
