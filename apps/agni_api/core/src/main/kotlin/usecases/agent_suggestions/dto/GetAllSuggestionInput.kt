package usecases.agent_suggestions.dto

import adapters.dto.QueryFilter
import domain.enums.AgentSuggestionStatusType

data class GetAllSuggestionInput(
    val query: QueryFilter,
    val status: domain.enums.AgentSuggestionStatusType? = null
)