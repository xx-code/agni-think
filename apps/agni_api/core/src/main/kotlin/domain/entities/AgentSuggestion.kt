package domain.entities

import domain.enums.AgentSuggestionStatusType
import domain.exceptions.ValidationException
import java.util.UUID

class AgentSuggestion(
    id: UUID = UUID.randomUUID(),
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
}