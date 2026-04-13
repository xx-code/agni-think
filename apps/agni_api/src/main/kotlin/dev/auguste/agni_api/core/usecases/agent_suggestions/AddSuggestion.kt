package dev.auguste.agni_api.core.usecases.agent_suggestions

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.AgentSuggestion
import dev.auguste.agni_api.core.entities.enums.AgentSuggestionStatusType
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.agent_suggestions.dto.AddSuggestionInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class AddSuggestion(
    private val agentSuggestionRepo: IRepository<AgentSuggestion>
): IUseCase<AddSuggestionInput, CreatedOutput> {
    override fun execAsync(input: AddSuggestionInput): CreatedOutput {
        val newSuggestion = AgentSuggestion(
            agentId = input.agentId,
            agentName = input.agentName,
            title = input.title,
            description = input.description,
            confidenceScore = input.confidenceScore,
            status = AgentSuggestionStatusType.PENDING
        )

        agentSuggestionRepo.create(newSuggestion)

        return CreatedOutput(newSuggestion.id)
    }
}