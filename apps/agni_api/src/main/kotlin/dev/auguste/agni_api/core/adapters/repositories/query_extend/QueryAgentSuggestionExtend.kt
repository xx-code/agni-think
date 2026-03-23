package dev.auguste.agni_api.core.adapters.repositories.query_extend

import dev.auguste.agni_api.core.adapters.repositories.IQueryExtend
import dev.auguste.agni_api.core.entities.AgentSuggestion
import dev.auguste.agni_api.core.entities.enums.AgentSuggestionStatusType

class QueryAgentSuggestionExtend(
    val status: AgentSuggestionStatusType?
): IQueryExtend<AgentSuggestion> {
    override fun isStatisfy(entity: AgentSuggestion): Boolean {
        return !(status !== null && status != entity.status)
    }
}