package usecases.agent_suggestions

import adapters.repositories.IRepository
import domain.entities.AgentSuggestion
import domain.exceptions.NotFoundException
import domain.enums.AgentSuggestionStatusType
import usecases.agent_suggestions.dto.ConfirmSuggestionInput
import usecases.interfaces.IUseCase

class ConfirmSuggestion(
    private val agentSuggestionRepo: IRepository<AgentSuggestion>
): IUseCase<ConfirmSuggestionInput, Unit> {
    override fun execAsync(input: ConfirmSuggestionInput) {
        val suggestion = agentSuggestionRepo.get(input.suggestionId) ?: throw NotFoundException.SingleEntity(input.suggestionId, "agent_suggestion")
        suggestion.status = if (input.isAccept)  _root_ide_package_.domain.enums.AgentSuggestionStatusType.ACCEPTED else _root_ide_package_.domain.enums.AgentSuggestionStatusType.REJECTED
        agentSuggestionRepo.update(suggestion)
    }
}