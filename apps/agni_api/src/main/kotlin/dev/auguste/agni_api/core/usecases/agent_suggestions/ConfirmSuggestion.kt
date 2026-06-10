package dev.auguste.agni_api.core.usecases.agent_suggestions

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.AgentSuggestion
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.enums.AgentSuggestionStatusType
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.agent_suggestions.dto.ConfirmSuggestionInput
import dev.auguste.agni_api.core.usecases.agent_suggestions.dto.GetSuggestionOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class ConfirmSuggestion(
    private val agentSuggestionRepo: IRepository<AgentSuggestion>
): IUseCase<ConfirmSuggestionInput, Unit> {
    override fun execAsync(input: ConfirmSuggestionInput) {
        val suggestion = agentSuggestionRepo.get(input.suggestionId) ?: throw DomainException.NotFound.("NO_AGENT_SUGGESTION_FOUND")
        suggestion.status = if (input.isAccept)  AgentSuggestionStatusType.ACCEPTED else AgentSuggestionStatusType.REJECTED
        agentSuggestionRepo.update(suggestion)
    }
}