package dev.auguste.agni_api.infras.usecase_configs

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.FinancePrinciple
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.finance_principles.CreateFinancePrinciple
import dev.auguste.agni_api.core.usecases.finance_principles.DeleteFinancePrinciple
import dev.auguste.agni_api.core.usecases.finance_principles.GetAllFinancePrinciple
import dev.auguste.agni_api.core.usecases.finance_principles.GetFinancePrinciple
import dev.auguste.agni_api.core.usecases.finance_principles.UpdateFinancePrinciple
import dev.auguste.agni_api.core.usecases.finance_principles.dto.CreateFinancePrincipleInput
import dev.auguste.agni_api.core.usecases.finance_principles.dto.DeleteFinancePrincipleInput
import dev.auguste.agni_api.core.usecases.finance_principles.dto.GetFinancePrincipleOutput
import dev.auguste.agni_api.core.usecases.finance_principles.dto.UpdateFinancePrincipleInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class FinancePrincipleConfig {

    @Bean
    fun createFinancePrinciple(
        financePrincipeRepo: IRepository<FinancePrinciple>
    ): IUseCase<CreateFinancePrincipleInput, CreatedOutput> {
        return CreateFinancePrinciple(financePrincipeRepo)
    }

    @Bean
    fun updateFinancePrinciple(
        financePrincipeRepo: IRepository<FinancePrinciple>
    ): IUseCase<UpdateFinancePrincipleInput, Unit> {
        return UpdateFinancePrinciple(financePrincipeRepo)
    }

    @Bean
    fun deleteFinancePrinciple(
        financePrincipeRepo: IRepository<FinancePrinciple>
    ): IUseCase<DeleteFinancePrincipleInput, Unit> {
        return DeleteFinancePrinciple(financePrincipeRepo)
    }

    @Bean
    fun getFinancePrinciple(
        financePrincipeRepo: IRepository<FinancePrinciple>
    ): IUseCase<UUID, GetFinancePrincipleOutput> {
        return GetFinancePrinciple(financePrincipeRepo)
    }

    @Bean
    fun getAllFinancePrinciple(
        financePrincipeRepo: IRepository<FinancePrinciple>
    ): IUseCase<QueryFilter, ListOutput<GetFinancePrincipleOutput>> {
        return GetAllFinancePrinciple(financePrincipeRepo)
    }
}