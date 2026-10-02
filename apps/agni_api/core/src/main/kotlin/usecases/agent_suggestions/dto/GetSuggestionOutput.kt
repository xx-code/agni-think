package usecases.agent_suggestions.dto

import domain.enums.AgentSuggestionStatusType
data class GetSuggestionOutput(
    val agentId: String,
    val agentName: String,
    val title: String,
    val description: String,
    val confidenceScore: Double,
    val status: domain.enums.AgentSuggestionStatusType
)
