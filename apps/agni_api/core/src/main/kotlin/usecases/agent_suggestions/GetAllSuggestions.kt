package usecases.agent_suggestions

import usecases.UseCase
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.AgentSuggestion
import usecases.dto.ListOutput
import usecases.agent_suggestions.dto.GetAllSuggestionInput
import usecases.agent_suggestions.dto.GetSuggestionOutput
class GetAllSuggestions(
    private val suggestionRepo: IRepository<AgentSuggestion>,
): UseCase<GetAllSuggestionInput, ListOutput<GetSuggestionOutput>>() {
    override suspend fun process(input: GetAllSuggestionInput): ListOutput<GetSuggestionOutput> {
        val condition = QueryExtendBuilder<AgentSuggestion>()
        if (input.status != null)
            condition.addCondition("status", QueryComparator.Equal, input.status.value)

        val suggestions = suggestionRepo.getAll(query = input.query, queryExtend = condition)

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