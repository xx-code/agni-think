package usecase_configs

import adapters.IAuthenticateAdapter
import adapters.repositories.IRepository
import domain.entities.Budget
import domain.entities.Profile
import domain.entities.Provision
import domain.entities.Fund
import domain.entities.ScheduleInvoice
import domain.entities.SpendingPeriod
import domain.entities.SpendingPeriodTemplate
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
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
import usecases.UseCase
import java.util.UUID

@Configuration
class SpendingPeriodConfig {

    @Bean
    fun createSpendingPeriod(
        spendingPeriodRepo: IRepository<SpendingPeriod>,
        spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
        auth: IAuthenticateAdapter
    ): UseCase<CreateSpendingPeriodInput, CreatedOutput> {
        return CreateSpendingPeriod(
            spendingPeriodRepo = spendingPeriodRepo,
            spendingPeriodTemplateRepo = spendingPeriodTemplateRepo,
            auth = auth
        )
    }

    @Bean
    fun updateSpendingPeriod(
        spendingPeriodRepo: IRepository<SpendingPeriod>,
        auth: IAuthenticateAdapter
    ): UseCase<UpdateSpendingPeriodInput, Unit> {
        return UpdateSpendingPeriod(
            spendingPeriodRepo = spendingPeriodRepo,
            auth = auth
        )
    }

    @Bean
    fun getSpendingPeriod(
        spendingPeriodRepo: IRepository<SpendingPeriod>,
        spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
        forcastSpendingPeriod: IUseCase<ForcastSpendingPeriodInput, ForcastSpendingPeriodOutput>,
        auth: IAuthenticateAdapter
    ): UseCase<UUID, GetSpendingPeriodOutput> {
        return GetSpendingPeriod(
            spendingPeriodRepo = spendingPeriodRepo,
            spendingPeriodTemplateRepo = spendingPeriodTemplateRepo,
            forcastSpendingPeriod = forcastSpendingPeriod,
            auth = auth
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
        getInvoice: IUseCase<GetAllInvoiceInput, ListOutput<GetInvoiceOutput>>,
        auth: IAuthenticateAdapter
    ): UseCase<ForcastSpendingPeriodInput, ForcastSpendingPeriodOutput> {
        return ForcastSpendingPeriod(
            scheduleInvoiceRepo = scheduleInvoiceRepo,
            budgetRepo = budgetRepo,
            profileRepo = profileRepo,
            getBalance = getBalance,
            getSavingBalance = getSavingBalance,
            getInvoices = getInvoice,
            provisionRepo = provisionRepo,
            fundRepo = fundRepo,
            auth = auth
        )
    }

    @Bean
    fun getAllSpendingPeriod(
        spendingPeriodRepo: IRepository<SpendingPeriod>,
        auth: IAuthenticateAdapter
    ): UseCase<GetAllSpendingPeriodInput, ListOutput<GetAllSpendingPeriodOutput>> {
        return GetAllSpendingPeriod(
            spendingPeriodRepo = spendingPeriodRepo,
            auth = auth
        )
    }

    @Bean("deleteSpendingPeriod")
    fun deleteSpendingPeriod(
        spendingPeriodRepo: IRepository<SpendingPeriod>,
        auth: IAuthenticateAdapter
    ): UseCase<UUID, Unit> {
        return DeleteSpendingPeriod(
            spendingPeriodRepo = spendingPeriodRepo,
            auth = auth
        )
    }

    @Bean("completeSpendingPeriod")
    fun completeSpendingPeriod(
        spendingPeriodRepo: IRepository<SpendingPeriod>,
        spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
        forcastSpendingPeriod: IUseCase<ForcastSpendingPeriodInput, ForcastSpendingPeriodOutput>,
        auth: IAuthenticateAdapter
    ): UseCase<UUID, Unit> {
        return CompleteSpendingPeriod(
            spendingPeriodRepo = spendingPeriodRepo,
            spendingPeriodTemplateRepo = spendingPeriodTemplateRepo,
            forcastSpendingPeriod = forcastSpendingPeriod,
            auth = auth
        )
    }
}
