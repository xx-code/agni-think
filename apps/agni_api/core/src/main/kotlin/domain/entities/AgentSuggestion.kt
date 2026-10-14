package domain.entities

import domain.enums.AgentSuggestionStatusType
import domain.exceptions.ValidationException
import java.util.UUID

class AgentSuggestion(
    id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val agentId: String,
    val agentName: String,
    title: String,
    description: String,
    confidenceScore: Double,
    status: AgentSuggestionStatusType,
): Entity(id) {
    var title by cleanObservable(title, this)
    var description by cleanObservable(description, this)
    var confidenceScore by cleanObservable(confidenceScore, this, {
        it in 0.0..100.0
    }) {
        ValidationException.AgentSuggestionInvalidConfidenceScore(it)
    }
    var status by cleanObservable(status, this)

    companion object {
        fun mock(
            id: UUID = UUID.randomUUID(),
            userId: UUID = UUID.randomUUID(),
            agentId: String = "agent-00",
            agentName: String = "agent",
            title: String = "title",
            description: String = "description",
            confidenceScore: Double = 100.0,
            status: AgentSuggestionStatusType = AgentSuggestionStatusType.PENDING,
        ): AgentSuggestion {
            return AgentSuggestion(
                id = id,
                userId = userId,
                agentId = agentId,
                agentName = agentName,
                title = title,
                description = description,
                confidenceScore = confidenceScore,
                status = status,
            )
        }
    }
}