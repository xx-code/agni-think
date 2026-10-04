package usecase_configs

import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.entities.Account
import domain.entities.Goal
import domain.entities.Fund
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.CreateInvoiceInput
import usecases.funds.CreateFund
import usecases.funds.DecreaseFund
import usecases.funds.DeleteFund
import usecases.funds.GetAllFunds
import usecases.funds.GetFund
import usecases.funds.IncreaseFund
import usecases.funds.UpdateFund
import usecases.funds.dto.CreateFundInput
import usecases.funds.dto.DecreaseSavingGoalInput
import usecases.funds.dto.DeleteFundInput
import usecases.funds.dto.GetAllFundInput
import usecases.funds.dto.GetSavingGoalOutput
import usecases.funds.dto.IncreaseSavingGoalInput
import usecases.funds.dto.UpdateFundInput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import usecases.UseCase
import java.util.UUID

@Configuration
class FundConfig {

    @Bean
    fun createFund(
        fundRepo: IRepository<Fund>,
        accountRepo: IRepository<Account>,
    ): UseCase<CreateFundInput, CreatedOutput> {
        return CreateFund(
            fundRepo = fundRepo,
            accountingRepo = accountRepo
        )
    }

    @Bean
    fun updateFund(
        fundRepo: IRepository<Fund>,
        accountRepo: IRepository<Account>,
    ): UseCase<UpdateFundInput, Unit> {
        return UpdateFund(
            fundRepo = fundRepo,
            accountRepo = accountRepo
        )
    }

    @Bean
    fun decreaseFund(
        fundRepo: IRepository<Fund>,
        accountRepo: IRepository<Account>,
        createInvoice: IUseCase<CreateInvoiceInput, CreatedOutput>,
        unitOfWork: IUnitOfWork,
    ): UseCase<DecreaseSavingGoalInput, Unit> {
        return DecreaseFund(
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
        createInvoice: IUseCase<CreateInvoiceInput, CreatedOutput>,
        unitOfWork: IUnitOfWork,
    ): UseCase<IncreaseSavingGoalInput, Unit> {
       return IncreaseFund(
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
        createInvoice: IUseCase<CreateInvoiceInput, CreatedOutput>,
        unitOfWork: IUnitOfWork,
   ): UseCase<DeleteFundInput, Unit> {
       return DeleteFund(
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
    ): UseCase<GetAllFundInput, ListOutput<GetSavingGoalOutput>> {
        return GetAllFunds(
            fundRepo = fundRepo,
            goalRepo = goalRepo
        )
    }

    @Bean
    fun getFund(
        fundRepo: IRepository<Fund>,
        goalRepo: IRepository<Goal>
    ): UseCase<UUID, GetSavingGoalOutput> {
        return GetFund(
            fundRepo = fundRepo,
            goalRepo = goalRepo
        )
    }
}