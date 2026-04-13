package dev.auguste.agni_api.core.usecases.agent_suggestions.dto

import dev.auguste.agni_api.core.entities.enums.AgentSuggestionStatusType
data class GetSuggestionOutput(
    val agentId: String,
    val agentName: String,
    val title: String,
    val description: String,
    val confidenceScore: Double,
    val status: AgentSuggestionStatusType
)
