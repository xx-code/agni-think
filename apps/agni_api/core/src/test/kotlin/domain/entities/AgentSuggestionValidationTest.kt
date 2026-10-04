package domain.entities

import kotlinx.coroutines.runBlocking

import domain.enums.AgentSuggestionStatusType
import domain.exceptions.ValidationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AgentSuggestionValidationTest {

    private fun suggestion(confidenceScore: Double = 80.0) = AgentSuggestion(
        agentId = "agent-1",
        agentName = "Budgeter",
        title = "Cut subscriptions",
        description = "You spend a lot on streaming",
        confidenceScore = confidenceScore,
        status = AgentSuggestionStatusType.PENDING,
    )

    @Test
    fun `accepts the lower and upper confidence boundaries`() = runBlocking {
        val suggestion = suggestion(confidenceScore = 0.0)
        suggestion.confidenceScore = 100.0

        assertEquals(100.0, suggestion.confidenceScore)
    }

    @Test
    fun `refuses a confidence below zero`() = runBlocking {
        val suggestion = suggestion()

        val error = assertFailsWith<ValidationException.AgentSuggestionInvalidConfidenceScore> {
            suggestion.confidenceScore = -0.1
        }

        assertEquals("AGENT_SUGGESTION_INVALID_CONFIDENCE_SCORE", error.errorKey)
        assertEquals(mapOf("confidence" to -0.1), error.metadata)
    }

    @Test
    fun `refuses a confidence above one hundred`() = runBlocking {
        val suggestion = suggestion()

        val error = assertFailsWith<ValidationException.AgentSuggestionInvalidConfidenceScore> {
            suggestion.confidenceScore = 100.1
        }

        assertEquals(mapOf("confidence" to 100.1), error.metadata)
    }
}