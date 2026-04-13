package dev.auguste.agni_api.core.usecases.agent_suggestions

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryAgentSuggestionExtend
import dev.auguste.agni_api.core.entities.AgentSuggestion
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.agent_suggestions.dto.GetAllSuggestionInput
import dev.auguste.agni_api.core.usecases.agent_suggestions.dto.GetSuggestionOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class GetAllSuggestions(
    private val suggestionRepo: IRepository<AgentSuggestion>,
): IUseCase<GetAllSuggestionInput, ListOutput<GetSuggestionOutput>> {
    override fun execAsync(input: GetAllSuggestionInput): ListOutput<GetSuggestionOutput> {
        val suggestions = suggestionRepo.getAll(query = input.query, queryExtend = QueryAgentSuggestionExtend(input.status))

        return ListOutput(
            items=suggestions.items.map {
                GetSuggestionOutput(
                    agentId = it.agentId,
                    agentName = it.agentName,
                    title = it.title,
                    description = it.description,
                    confidenceScore = it.confidenceScore,
                    status = it.status,
                )
            },
            total=suggestions.total,
        )
    }

}