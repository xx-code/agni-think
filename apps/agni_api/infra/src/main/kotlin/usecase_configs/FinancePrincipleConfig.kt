package usecase_configs

import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import domain.entities.FinancePrinciple
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
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
import usecases.UseCase
import java.util.UUID

@Configuration
class FinancePrincipleConfig {

    @Bean
    fun createFinancePrinciple(
        financePrincipeRepo: IRepository<FinancePrinciple>
    ): UseCase<CreateFinancePrincipleInput, CreatedOutput> {
        return CreateFinancePrinciple(financePrincipeRepo)
    }

    @Bean
    fun updateFinancePrinciple(
        financePrincipeRepo: IRepository<FinancePrinciple>
    ): UseCase<UpdateFinancePrincipleInput, Unit> {
        return UpdateFinancePrinciple(financePrincipeRepo)
    }

    @Bean
    fun deleteFinancePrinciple(
        financePrincipeRepo: IRepository<FinancePrinciple>
    ): UseCase<DeleteFinancePrincipleInput, Unit> {
        return DeleteFinancePrinciple(financePrincipeRepo)
    }

    @Bean
    fun getFinancePrinciple(
        financePrincipeRepo: IRepository<FinancePrinciple>
    ): UseCase<UUID, GetFinancePrincipleOutput> {
        return GetFinancePrinciple(financePrincipeRepo)
    }

    @Bean
    fun getAllFinancePrinciple(
        financePrincipeRepo: IRepository<FinancePrinciple>
    ): UseCase<QueryFilter, ListOutput<GetFinancePrincipleOutput>> {
        return GetAllFinancePrinciple(financePrincipeRepo)
    }
}