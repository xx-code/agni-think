package usecase_configs

import adapters.repositories.IRepository
import domain.entities.AgentSuggestion
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
import usecases.agent_suggestions.AddSuggestion
import usecases.agent_suggestions.ConfirmSuggestion
import usecases.agent_suggestions.GetAllSuggestions
import usecases.agent_suggestions.dto.AddSuggestionInput
import usecases.agent_suggestions.dto.ConfirmSuggestionInput
import usecases.agent_suggestions.dto.GetAllSuggestionInput
import usecases.agent_suggestions.dto.GetSuggestionOutput
import usecases.interfaces.IUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import usecases.UseCase


@Configuration
class AgentSuggestionConfig {
    @Bean
    fun addAgentSuggestion(
        agentSuggestionRepo: IRepository<AgentSuggestion>,
    ): UseCase<AddSuggestionInput, CreatedOutput> {
        return AddSuggestion(
            agentSuggestionRepo
        )
    }

    @Bean
    fun confirmAgentSuggestion(
        agentSuggestionRepo: IRepository<AgentSuggestion>,
    ): UseCase<ConfirmSuggestionInput, Unit> {
        return ConfirmSuggestion(agentSuggestionRepo)
    }

    @Bean
    fun getAllAgentSuggestions(
        agentSuggestionRepo: IRepository<AgentSuggestion>,
    ): UseCase<GetAllSuggestionInput, ListOutput<GetSuggestionOutput>> {
        return GetAllSuggestions(agentSuggestionRepo)
    }
}