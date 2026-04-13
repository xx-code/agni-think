package dev.auguste.agni_api.core.usecases.agent_suggestions.dto

data class AddSuggestionInput(
    val agentId: String,
    val agentName: String,
    val title: String,
    val description: String,
    val confidenceScore: Double
)