package dev.auguste.agni_api.infras.usecase_configs

import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import domain.entities.FinancePrinciple
import usecases.CreatedOutput
import usecases.ListOutput
import usecases.finance_principles.CreateFinancePrinciple
import usecases.finance_principles.DeleteFinancePrinciple
import usecases.finance_principles.GetAllFinancePrinciple
import usecases.finance_principles.GetFinancePrinciple
import usecases.finance_principles.UpdateFinancePrinciple
import usecases.finance_principles.dto.CreateFinancePrincipleInput
import usecases.finance_principles.dto.DeleteFinancePrincipleInput
import usecases.finance_principles.dto.GetFinancePrincipleOutput
import usecases.finance_principles.dto.UpdateFinancePrincipleInput
import usecases.interfaces.IUseCase
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