package adapters.repositories.query_extend

import adapters.repositories.IQueryExtend
import domain.entities.AgentSuggestion
import domain.enums.AgentSuggestionStatusType

class QueryAgentSuggestionExtend(
    val status: domain.enums.AgentSuggestionStatusType?
): IQueryExtend<domain.entities.AgentSuggestion> {
    override fun isStatisfy(entity: domain.entities.AgentSuggestion): Boolean {
        return !(status !== null && status != entity.status)
    }
}