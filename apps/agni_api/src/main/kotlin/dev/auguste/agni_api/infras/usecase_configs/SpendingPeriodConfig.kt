package dev.auguste.agni_api.infras.usecase_configs

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.entities.Profile
import dev.auguste.agni_api.core.entities.Provision
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.entities.SpendingPeriod
import dev.auguste.agni_api.core.entities.SpendingPeriodTemplate
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSavingBalanceInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetAllInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetInvoiceOutput
import dev.auguste.agni_api.core.usecases.spending_period.CompleteSpendingPeriod
import dev.auguste.agni_api.core.usecases.spending_period.CreateSpendingPeriod
import dev.auguste.agni_api.core.usecases.spending_period.DeleteSpendingPeriod
import dev.auguste.agni_api.core.usecases.spending_period.ForcastSpendingPeriod
import dev.auguste.agni_api.core.usecases.spending_period.GetAllSpendingPeriod
import dev.auguste.agni_api.core.usecases.spending_period.GetSpendingPeriod
import dev.auguste.agni_api.core.usecases.spending_period.UpdateSpendingPeriod
import dev.auguste.agni_api.core.usecases.spending_period.dto.CreateSpendingPeriodInput
import dev.auguste.agni_api.core.usecases.spending_period.dto.ForcastSpendingPeriodInput
import dev.auguste.agni_api.core.usecases.spending_period.dto.ForcastSpendingPeriodOutput
import dev.auguste.agni_api.core.usecases.spending_period.dto.GetAllSpendingPeriodInput
import dev.auguste.agni_api.core.usecases.spending_period.dto.GetAllSpendingPeriodOutput
import dev.auguste.agni_api.core.usecases.spending_period.dto.GetSpendingPeriodOutput
import dev.auguste.agni_api.core.usecases.spending_period.dto.UpdateSpendingPeriodInput
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
        fundRepo: IRepository<SavingGoal>,
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
