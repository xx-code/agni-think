package dev.auguste.agni_api.core.usecases.agent_suggestions.dto

import java.util.UUID

data class ConfirmSuggestionInput(
    val suggestionId: UUID,
    val isAccept: Boolean
)