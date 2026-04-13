package dev.auguste.agni_api.controllers.models

import dev.auguste.agni_api.core.usecases.agent_suggestions.dto.AddSuggestionInput

data class ApiAddSuggestionModel(
    val agentId: String,
    val agentName: String,
    val title: String,
    val description: String,
    val confidenceScore: Double
)

fun mapApiAddSuggestionModel(model: ApiAddSuggestionModel): AddSuggestionInput {
    return AddSuggestionInput(
        agentId = model.agentId,
        agentName = model.agentName,
        title = model.title,
        description = model.description,
        confidenceScore = model.confidenceScore
    )
}