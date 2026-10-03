package dev.auguste.agni_api.infras.usecase_configs

import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.entities.Account
import domain.entities.Goal
import domain.entities.Fund
import usecases.CreatedOutput
import usecases.ListOutput
import usecases.interfaces.IInnerUseCase
import usecases.interfaces.IUseCase
import usecases.invoices.dto.CreateInvoiceInput
import usecases.saving_goals.CreateSavingGoal
import usecases.saving_goals.DecreaseSavingGoal
import usecases.saving_goals.DeleteSavingGoal
import usecases.saving_goals.GetAllSavingGoal
import usecases.saving_goals.GetSavingGoal
import usecases.saving_goals.IncreaseSavingGoal
import usecases.saving_goals.UpdateSavingGoal
import usecases.saving_goals.dto.CreateSavingGoalInput
import usecases.saving_goals.dto.DecreaseSavingGoalInput
import usecases.saving_goals.dto.DeleteSavingGoalInput
import usecases.saving_goals.dto.GetAllSavingGoalInput
import usecases.saving_goals.dto.GetSavingGoalOutput
import usecases.saving_goals.dto.IncreaseSavingGoalInput
import usecases.saving_goals.dto.UpdateSavingGoalInput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class FundConfig {

    @Bean
    fun createFund(
        fundRepo: IRepository<Fund>,
        accountRepo: IRepository<Account>,
    ): IUseCase<CreateSavingGoalInput, CreatedOutput> {
        return CreateSavingGoal(
            fundRepo = fundRepo,
            accountingRepo = accountRepo
        )
    }

    @Bean
    fun updateFund(
        fundRepo: IRepository<Fund>,
        accountRepo: IRepository<Account>,
    ): IUseCase<UpdateSavingGoalInput, Unit> {
        return UpdateSavingGoal(
            fundRepo = fundRepo,
            accountRepo = accountRepo
        )
    }

    @Bean
    fun decreaseFund(
        fundRepo: IRepository<Fund>,
        accountRepo: IRepository<Account>,
        createInvoice: IInnerUseCase<CreateInvoiceInput, CreatedOutput>,
        unitOfWork: IUnitOfWork,
    ): IInnerUseCase<DecreaseSavingGoalInput, Unit> {
        return DecreaseSavingGoal(
            fundRepo = fundRepo,
            accountRepo = accountRepo,
            createInvoice = createInvoice,
            unitOfWork = unitOfWork
        )
    }

    @Bean
    fun increaseFund(
        fundRepo: IRepository<Fund>,
        accountRepo: IRepository<Account>,
        createInvoice: IInnerUseCase<CreateInvoiceInput, CreatedOutput>,
        unitOfWork: IUnitOfWork,
    ): IUseCase<IncreaseSavingGoalInput, Unit> {
       return IncreaseSavingGoal(
           fundRepo = fundRepo,
           accountRepo = accountRepo,
           createInvoice = createInvoice,
           unitOfWork = unitOfWork
       )
    }

    @Bean
   fun deleteFund(
        fundRepo: IRepository<Fund>,
        accountRepo: IRepository<Account>,
        goalRepo: IRepository<Goal>,
        createInvoice: IInnerUseCase<CreateInvoiceInput, CreatedOutput>,
        unitOfWork: IUnitOfWork,
   ): IUseCase<DeleteSavingGoalInput, Unit> {
       return DeleteSavingGoal(
           fundRepo = fundRepo,
           accountRepo = accountRepo,
           createInvoice = createInvoice,
           unitOfWork = unitOfWork,
           goalRepo = goalRepo
       )
   }

    @Bean
    fun getAllFund(
        fundRepo: IRepository<Fund>,
        goalRepo: IRepository<Goal>
    ): IUseCase<GetAllSavingGoalInput, ListOutput<GetSavingGoalOutput>> {
        return GetAllSavingGoal(
            fundRepo = fundRepo,
            goalRepo = goalRepo
        )
    }

    @Bean
    fun getFund(
        fundRepo: IRepository<Fund>,
        goalRepo: IRepository<Goal>
    ): IUseCase<UUID, GetSavingGoalOutput> {
        return GetSavingGoal(
            fundRepo = fundRepo,
            goalRepo = goalRepo
        )
    }
}