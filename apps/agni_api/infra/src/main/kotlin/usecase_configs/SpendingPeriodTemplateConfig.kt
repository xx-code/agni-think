package usecase_configs

import adapters.IAuthenticateAdapter
import adapters.dto.QueryFilter
import adapters.events.IEventRegister
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.entities.Budget
import domain.entities.Profile
import domain.entities.ScheduleInvoice
import domain.entities.SpendingPeriod

import domain.entities.SpendingPeriodTemplate
import domain.entities.User
import usecases.dto.BackgroundTaskOut
import usecases.dto.CreatedOutput
import usecases.dto.ListOutput
import usecases.interfaces.IUseCase
import usecases.spending_period.dto.ForcastSpendingPeriodInput
import usecases.spending_period.dto.ForcastSpendingPeriodOutput
import usecases.spending_period_template.ApplySpendingPeriodTemplate
import usecases.spending_period_template.CreateSpendingPeriodTemplate
import usecases.spending_period_template.DeleteSpendingPeriodTemplate
import usecases.spending_period_template.GetAllSpendingPeriodTemplate
import usecases.spending_period_template.GetSpendingPeriodTemplate
import usecases.spending_period_template.UpdateSpendingPeriodTemplate
import usecases.spending_period_template.dto.CreateSpendingPeriodTemplateInput
import usecases.spending_period_template.dto.GetSpendingPeriodTemplateOutput
import usecases.spending_period_template.dto.UpdateSpendingPeriodTemplateInput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import usecases.UseCase
import usecases.spending_period_template.dto.GetAllSpendingPeriodTemplateInput
import java.util.UUID

@Configuration
class SpendingPeriodTemplateConfig {

    @Bean
    fun createSpendingPeriodTemplate(
        spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
        budgetRepo: IRepository<Budget>,
        auth: IAuthenticateAdapter
    ): UseCase<CreateSpendingPeriodTemplateInput, CreatedOutput> {
        return CreateSpendingPeriodTemplate(
            spendingPeriodTemplateRepo = spendingPeriodTemplateRepo,
            budgetRepo = budgetRepo,
            auth = auth
        )
    }

    @Bean
    fun updateSpendingPeriodTemplate(
        spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
        budgetRepo: IRepository<Budget>,
        auth: IAuthenticateAdapter
    ): UseCase<UpdateSpendingPeriodTemplateInput, Unit> {
        return UpdateSpendingPeriodTemplate(
            spendingPeriodTemplateRepo = spendingPeriodTemplateRepo,
            budgetRepo = budgetRepo,
            auth = auth
        )
    }

    @Bean
    fun getSpendingPeriodTemplate(
        spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
        budgetRepo: IRepository<Budget>,
        auth: IAuthenticateAdapter
    ): UseCase<UUID, GetSpendingPeriodTemplateOutput> {
        return GetSpendingPeriodTemplate(
            spendingPeriodTemplateRepo = spendingPeriodTemplateRepo,
            budgetRepo = budgetRepo,
            auth = auth
        )
    }

    @Bean
    fun getAllSpendingPeriodTemplate(
        spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
        budgetRepo: IRepository<Budget>,
        auth: IAuthenticateAdapter
    ): UseCase<GetAllSpendingPeriodTemplateInput, ListOutput<GetSpendingPeriodTemplateOutput>> {
        return GetAllSpendingPeriodTemplate(
            spendingPeriodTemplateRepo = spendingPeriodTemplateRepo,
            budgetRepo = budgetRepo,
            auth = auth
        )
    }

    @Bean("deleteSpendingPeriodTemplate")
    fun deleteSpendingPeriodTemplate(
        spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
        auth: IAuthenticateAdapter
    ) : UseCase<UUID, Unit> {
       return DeleteSpendingPeriodTemplate(
           spendingPeriodTemplateRepo = spendingPeriodTemplateRepo,
           auth = auth
       )
    }

    @Bean("applySpendingPeriodTemplate")
    fun applySpendingPeriodTemplate(
        spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
        spendingPeriodRepo: IRepository<SpendingPeriod>,
        profileRepo: IRepository<Profile>,
        scheduleInvoicesRepo: IRepository<ScheduleInvoice>,
        eventRegister: IEventRegister,
        unitOfWork: IUnitOfWork,
    ): UseCase<Unit, BackgroundTaskOut> {
        return ApplySpendingPeriodTemplate(
            spendingPeriodTemplateRepo = spendingPeriodTemplateRepo,
            spendingPeriodRepo = spendingPeriodRepo,
            profileRepo = profileRepo,
            unitOfWork = unitOfWork,
            scheduleInvoiceRepo = scheduleInvoicesRepo,
            eventRegister = eventRegister,
        )
    }
}