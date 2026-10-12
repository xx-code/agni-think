package usecases.agent_suggestions

import usecases.UseCase
import adapters.IAuthenticateAdapter
import adapters.repositories.IRepository
import domain.entities.AgentSuggestion
import domain.entities.User
import domain.enums.AgentSuggestionStatusType
import usecases.dto.CreatedOutput
import usecases.agent_suggestions.dto.AddSuggestionInput
class AddSuggestion(
    private val agentSuggestionRepo: IRepository<AgentSuggestion>,
    auth: IAuthenticateAdapter
): UseCase<AddSuggestionInput, CreatedOutput>(auth = auth) {
    override suspend fun process(input: AddSuggestionInput): CreatedOutput {
        val currentUser = getCurrentUser()

        val newSuggestion = AgentSuggestion(
            userId = currentUser.id,
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