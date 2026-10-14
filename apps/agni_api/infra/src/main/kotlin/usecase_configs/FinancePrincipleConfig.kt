package usecase_configs

import adapters.IAuthenticateAdapter
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import domain.entities.FinancePrinciple
import domain.entities.User
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
import usecases.finance_principles.dto.GetAllFinancePrincipleInput
import java.util.UUID

@Configuration
class FinancePrincipleConfig {

    @Bean
    fun createFinancePrinciple(
        financePrincipeRepo: IRepository<FinancePrinciple>,
        auth: IAuthenticateAdapter
    ): UseCase<CreateFinancePrincipleInput, CreatedOutput> {
        return CreateFinancePrinciple(financePrincipleRepo = financePrincipeRepo, auth = auth)
    }

    @Bean
    fun updateFinancePrinciple(
        financePrincipeRepo: IRepository<FinancePrinciple>,
        auth: IAuthenticateAdapter
    ): UseCase<UpdateFinancePrincipleInput, Unit> {
        return UpdateFinancePrinciple(financePrincipleRepo = financePrincipeRepo, auth = auth)
    }

    @Bean
    fun deleteFinancePrinciple(
        financePrincipeRepo: IRepository<FinancePrinciple>,
        auth: IAuthenticateAdapter
    ): UseCase<DeleteFinancePrincipleInput, Unit> {
        return DeleteFinancePrinciple(financePrincipleRepo = financePrincipeRepo, auth = auth)
    }

    @Bean
    fun getFinancePrinciple(
        financePrincipeRepo: IRepository<FinancePrinciple>,
        auth: IAuthenticateAdapter
    ): UseCase<UUID, GetFinancePrincipleOutput> {
        return GetFinancePrinciple(financePrincipleRepo = financePrincipeRepo, auth = auth)
    }

    @Bean
    fun getAllFinancePrinciple(
        financePrincipeRepo: IRepository<FinancePrinciple>,
        auth: IAuthenticateAdapter
    ): UseCase<GetAllFinancePrincipleInput, ListOutput<GetFinancePrincipleOutput>> {
        return GetAllFinancePrinciple(financePrincipleRepo = financePrincipeRepo, auth = auth)
    }
}