package dev.auguste.agni_api.core.usecases.agent_suggestions.dto

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.entities.enums.AgentSuggestionStatusType

data class GetAllSuggestionInput(
    val query: QueryFilter,
    val status: AgentSuggestionStatusType? = null
)