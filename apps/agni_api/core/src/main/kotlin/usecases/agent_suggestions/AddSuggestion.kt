package usecases.agent_suggestions

import adapters.repositories.IRepository
import domain.entities.AgentSuggestion
import domain.enums.AgentSuggestionStatusType
import usecases.CreatedOutput
import usecases.agent_suggestions.dto.AddSuggestionInput
import usecases.interfaces.IUseCase

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