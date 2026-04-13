package dev.auguste.agni_api.infras.usecase_configs

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.AgentSuggestion
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.agent_suggestions.AddSuggestion
import dev.auguste.agni_api.core.usecases.agent_suggestions.ConfirmSuggestion
import dev.auguste.agni_api.core.usecases.agent_suggestions.GetAllSuggestions
import dev.auguste.agni_api.core.usecases.agent_suggestions.dto.AddSuggestionInput
import dev.auguste.agni_api.core.usecases.agent_suggestions.dto.ConfirmSuggestionInput
import dev.auguste.agni_api.core.usecases.agent_suggestions.dto.GetAllSuggestionInput
import dev.auguste.agni_api.core.usecases.agent_suggestions.dto.GetSuggestionOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
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