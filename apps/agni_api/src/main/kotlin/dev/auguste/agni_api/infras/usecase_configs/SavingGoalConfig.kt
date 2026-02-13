package dev.auguste.agni_api.infras.usecase_configs

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IInnerUseCase
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.CreateInvoiceInput
import dev.auguste.agni_api.core.usecases.saving_goals.CreateSavingGoal
import dev.auguste.agni_api.core.usecases.saving_goals.DecreaseSavingGoal
import dev.auguste.agni_api.core.usecases.saving_goals.DeleteSavingGoal
import dev.auguste.agni_api.core.usecases.saving_goals.GetAllSavingGoal
import dev.auguste.agni_api.core.usecases.saving_goals.GetSavingGoal
import dev.auguste.agni_api.core.usecases.saving_goals.IncreaseSavingGoal
import dev.auguste.agni_api.core.usecases.saving_goals.UpdateSavingGoal
import dev.auguste.agni_api.core.usecases.saving_goals.dto.CreateSavingGoalInput
import dev.auguste.agni_api.core.usecases.saving_goals.dto.DecreaseSavingGoalInput
import dev.auguste.agni_api.core.usecases.saving_goals.dto.DeleteSavingGoalInput
import dev.auguste.agni_api.core.usecases.saving_goals.dto.GetSavingGoalOutput
import dev.auguste.agni_api.core.usecases.saving_goals.dto.IncreaseSavingGoalInput
import dev.auguste.agni_api.core.usecases.saving_goals.dto.UpdateSavingGoalInput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class SavingGoalConfig {

    @Bean
    fun createSavingGoal(
        savingGoalRepo: IRepository<SavingGoal>,
        accountRepo: IRepository<Account>,
    ): IUseCase<CreateSavingGoalInput, CreatedOutput> {
        return CreateSavingGoal(
            savingGoalRepo = savingGoalRepo,
            accountingRepo = accountRepo
        )
    }

    @Bean
    fun updateSavingGoal(
        savingGoalRepo: IRepository<SavingGoal>,
        accountRepo: IRepository<Account>,
    ): IUseCase<UpdateSavingGoalInput, Unit> {
        return UpdateSavingGoal(
            savingGoalRepo = savingGoalRepo,
            accountRepo = accountRepo
        )
    }

    @Bean
    fun decreaseSavingGoal(
        savingGoalRepo: IRepository<SavingGoal>,
        accountRepo: IRepository<Account>,
        createInvoice: IInnerUseCase<CreateInvoiceInput, CreatedOutput>,
        unitOfWork: IUnitOfWork,
    ): IUseCase<DecreaseSavingGoalInput, Unit> {
        return DecreaseSavingGoal(
            savingGoalRepo = savingGoalRepo,
            accountRepo = accountRepo,
            createInvoice = createInvoice,
            unitOfWork = unitOfWork
        )
    }

    @Bean
    fun increaseSavingGoal(
        savingGoalRepo: IRepository<SavingGoal>,
        accountRepo: IRepository<Account>,
        createInvoice: IInnerUseCase<CreateInvoiceInput, CreatedOutput>,
        unitOfWork: IUnitOfWork,
    ): IUseCase<IncreaseSavingGoalInput, Unit> {
       return IncreaseSavingGoal(
           savingGoalRepo = savingGoalRepo,
           accountRepo = accountRepo,
           createInvoice = createInvoice,
           unitOfWork = unitOfWork
       )
    }

    @Bean
   fun deleteSavingGoal(
       savingGoalRepo: IRepository<SavingGoal>,
       accountRepo: IRepository<Account>,
       createInvoice: IInnerUseCase<CreateInvoiceInput, CreatedOutput>,
       unitOfWork: IUnitOfWork,
   ): IUseCase<DeleteSavingGoalInput, Unit> {
       return DeleteSavingGoal(
           savingGoalRepo = savingGoalRepo,
           accountRepo = accountRepo,
           createInvoice = createInvoice,
           unitOfWork = unitOfWork
       )
   }

    @Bean
    fun getAllSavingGoals(
        savingGoalRepo: IRepository<SavingGoal>,
    ): IUseCase<QueryFilter, ListOutput<GetSavingGoalOutput>> {
        return GetAllSavingGoal(
            savingGoalRepo = savingGoalRepo,
        )
    }

    @Bean
    fun getSavingGoal(
        savingGoalRepo: IRepository<SavingGoal>,
    ): IUseCase<UUID, GetSavingGoalOutput> {
        return GetSavingGoal(
            savingGoalRepo = savingGoalRepo
        )
    }
}