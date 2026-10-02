package dev.auguste.agni_api.infras.usecase_configs

import adapters.repositories.IRepository
import domain.entities.AgentSuggestion
import usecases.CreatedOutput
import usecases.ListOutput
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


@Configuration
class AgentSuggestionConfig {
    @Bean
    fun addAgentSuggestion(
        agentSuggestionRepo: IRepository<AgentSuggestion>,
    ): IUseCase<AddSuggestionInput, CreatedOutput> {
        return AddSuggestion(
            agentSuggestionRepo
        )
    }

    @Bean
    fun confirmAgentSuggestion(
        agentSuggestionRepo: IRepository<AgentSuggestion>,
    ): IUseCase<ConfirmSuggestionInput, Unit> {
        return ConfirmSuggestion(agentSuggestionRepo)
    }

    @Bean
    fun getAllAgentSuggestions(
        agentSuggestionRepo: IRepository<AgentSuggestion>,
    ): IUseCase<GetAllSuggestionInput, ListOutput<GetSuggestionOutput>> {
        return GetAllSuggestions(agentSuggestionRepo)
    }
}