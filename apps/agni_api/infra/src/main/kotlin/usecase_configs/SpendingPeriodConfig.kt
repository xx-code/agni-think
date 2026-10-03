package usecase_configs

import adapters.repositories.IRepository
import domain.entities.Budget
import domain.entities.Profile
import domain.entities.Provision
import domain.entities.Fund
import domain.entities.ScheduleInvoice
import domain.entities.SpendingPeriod
import domain.entities.SpendingPeriodTemplate
import usecases.CreatedOutput
import usecases.ListOutput
import usecases.analystics.dto.GetSavingBalanceInput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetAllInvoiceInput
import usecases.invoices.dto.GetBalanceInput
import usecases.invoices.dto.GetBalanceOutput
import usecases.invoices.dto.GetInvoiceOutput
import usecases.spending_period.CompleteSpendingPeriod
import usecases.spending_period.CreateSpendingPeriod
import usecases.spending_period.DeleteSpendingPeriod
import usecases.spending_period.ForcastSpendingPeriod
import usecases.spending_period.GetAllSpendingPeriod
import usecases.spending_period.GetSpendingPeriod
import usecases.spending_period.UpdateSpendingPeriod
import usecases.spending_period.dto.CreateSpendingPeriodInput
import usecases.spending_period.dto.ForcastSpendingPeriodInput
import usecases.spending_period.dto.ForcastSpendingPeriodOutput
import usecases.spending_period.dto.GetAllSpendingPeriodInput
import usecases.spending_period.dto.GetAllSpendingPeriodOutput
import usecases.spending_period.dto.GetSpendingPeriodOutput
import usecases.spending_period.dto.UpdateSpendingPeriodInput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class SpendingPeriodConfig {

    @Bean
    fun createSpendingPeriod(
        spendingPeriodRepo: IRepository<SpendingPeriod>,
        spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>
    ): IUseCase<CreateSpendingPeriodInput, CreatedOutput> {
        return CreateSpendingPeriod(
            spendingPeriodRepo = spendingPeriodRepo,
            spendingPeriodTemplateRepo = spendingPeriodTemplateRepo
        )
    }

    @Bean
    fun updateSpendingPeriod(
        spendingPeriodRepo: IRepository<SpendingPeriod>
    ): IUseCase<UpdateSpendingPeriodInput, Unit> {
        return UpdateSpendingPeriod(
            spendingPeriodRepo = spendingPeriodRepo
        )
    }

    @Bean
    fun getSpendingPeriod(
        spendingPeriodRepo: IRepository<SpendingPeriod>,
        spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
        forcastSpendingPeriod: IUseCase<ForcastSpendingPeriodInput, ForcastSpendingPeriodOutput>
    ): IUseCase<UUID, GetSpendingPeriodOutput> {
        return GetSpendingPeriod(
            spendingPeriodRepo = spendingPeriodRepo,
            spendingPeriodTemplateRepo = spendingPeriodTemplateRepo,
            forcastSpendingPeriod = forcastSpendingPeriod
        )
    }


    @Bean
    fun forcastSpendingPeriod(
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        budgetRepo: IRepository<Budget>,
        profileRepo: IRepository<Profile>,
        provisionRepo: IRepository<Provision>,
        fundRepo: IRepository<Fund>,
        getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
        getSavingBalance: IUseCase<GetSavingBalanceInput, Double>,
        getInvoice: IUseCase<GetAllInvoiceInput, ListOutput<GetInvoiceOutput>>
    ): IUseCase<ForcastSpendingPeriodInput, ForcastSpendingPeriodOutput> {
        return ForcastSpendingPeriod(
            scheduleInvoiceRepo = scheduleInvoiceRepo,
            budgetRepo = budgetRepo,
            profileRepo = profileRepo,
            getBalance = getBalance,
            getSavingBalance = getSavingBalance,
            getInvoices = getInvoice,
            provisionRepo = provisionRepo,
            fundRepo = fundRepo,
        )
    }

    @Bean
    fun getAllSpendingPeriod(
        spendingPeriodRepo: IRepository<SpendingPeriod>
    ): IUseCase<GetAllSpendingPeriodInput, ListOutput<GetAllSpendingPeriodOutput>> {
        return GetAllSpendingPeriod(
            spendingPeriodRepo = spendingPeriodRepo
        )
    }

    @Bean("deleteSpendingPeriod")
    fun deleteSpendingPeriod(
        spendingPeriodRepo: IRepository<SpendingPeriod>
    ): IUseCase<UUID, Unit> {
        return DeleteSpendingPeriod(
            spendingPeriodRepo = spendingPeriodRepo
        )
    }

    @Bean("completeSpendingPeriod")
    fun completeSpendingPeriod(
        spendingPeriodRepo: IRepository<SpendingPeriod>,
        spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
        forcastSpendingPeriod: IUseCase<ForcastSpendingPeriodInput, ForcastSpendingPeriodOutput>
    ): IUseCase<UUID, Unit> {
        return CompleteSpendingPeriod(
            spendingPeriodRepo = spendingPeriodRepo,
            spendingPeriodTemplateRepo = spendingPeriodTemplateRepo,
            forcastSpendingPeriod = forcastSpendingPeriod
        )
    }
}
