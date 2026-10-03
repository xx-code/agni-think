package usecases.agent_suggestions

import usecases.UseCase
import adapters.repositories.IRepository
import domain.entities.AgentSuggestion
import domain.exceptions.NotFoundException
import domain.enums.AgentSuggestionStatusType
import usecases.agent_suggestions.dto.ConfirmSuggestionInput
class ConfirmSuggestion(
    private val agentSuggestionRepo: IRepository<AgentSuggestion>
): UseCase<ConfirmSuggestionInput, Unit>() {
    override suspend fun process(input: ConfirmSuggestionInput) {
        val suggestion = agentSuggestionRepo.get(input.suggestionId) ?: throw NotFoundException.SingleEntity(input.suggestionId, "agent_suggestion")
        suggestion.status = if (input.isAccept)  AgentSuggestionStatusType.ACCEPTED else AgentSuggestionStatusType.REJECTED
        agentSuggestionRepo.update(suggestion)
    }
}