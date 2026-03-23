package dev.auguste.agni_api.core.entities

import dev.auguste.agni_api.core.entities.enums.AgentSuggestionStatusType
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
    var confidenceScore by cleanObservable(confidenceScore, this)
    var status by cleanObservable(status, this)
}