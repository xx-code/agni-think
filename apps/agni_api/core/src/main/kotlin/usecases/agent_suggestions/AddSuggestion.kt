package usecases.agent_suggestions

import usecases.UseCase
import adapters.repositories.IRepository
import domain.entities.AgentSuggestion
import domain.enums.AgentSuggestionStatusType
import usecases.dto.CreatedOutput
import usecases.agent_suggestions.dto.AddSuggestionInput
class AddSuggestion(
    private val agentSuggestionRepo: IRepository<AgentSuggestion>
): UseCase<AddSuggestionInput, CreatedOutput>() {
    override suspend fun process(input: AddSuggestionInput): CreatedOutput {
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