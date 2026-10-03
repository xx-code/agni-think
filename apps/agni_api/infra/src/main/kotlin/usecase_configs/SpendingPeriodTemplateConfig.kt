package usecase_configs

import adapters.dto.QueryFilter
import adapters.events.IEventRegister
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import domain.entities.Budget
import domain.entities.Profile
import domain.entities.SpendingPeriod

import domain.entities.SpendingPeriodTemplate
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
import java.util.UUID

@Configuration
class SpendingPeriodTemplateConfig {

    @Bean
    fun createSpendingPeriodTemplate(
        spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
        budgetRepo: IRepository<Budget>
    ): UseCase<CreateSpendingPeriodTemplateInput, CreatedOutput> {
        return CreateSpendingPeriodTemplate(
            spendingPeriodTemplateRepo = spendingPeriodTemplateRepo,
            budgetRepo = budgetRepo,
        )
    }

    @Bean
    fun updateSpendingPeriodTemplate(
        spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
        budgetRepo: IRepository<Budget>
    ): UseCase<UpdateSpendingPeriodTemplateInput, Unit> {
        return UpdateSpendingPeriodTemplate(
            spendingPeriodTemplateRepo = spendingPeriodTemplateRepo,
            budgetRepo = budgetRepo,
        )
    }

    @Bean
    fun getSpendingPeriodTemplate(
        spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
        budgetRepo: IRepository<Budget>
    ): UseCase<UUID, GetSpendingPeriodTemplateOutput> {
        return GetSpendingPeriodTemplate(
            spendingPeriodTemplateRepo = spendingPeriodTemplateRepo,
            budgetRepo = budgetRepo
        )
    }

    @Bean
    fun getAllSpendingPeriodTemplate(
        spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
        budgetRepo: IRepository<Budget>
    ): UseCase<QueryFilter, ListOutput<GetSpendingPeriodTemplateOutput>> {
        return GetAllSpendingPeriodTemplate(
            spendingPeriodTemplateRepo = spendingPeriodTemplateRepo,
            budgetRepo = budgetRepo,
        )
    }

    @Bean("deleteSpendingPeriodTemplate")
    fun deleteSpendingPeriodTemplate(
        spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
    ) : UseCase<UUID, Unit> {
       return DeleteSpendingPeriodTemplate(
           spendingPeriodTemplateRepo = spendingPeriodTemplateRepo
       )
    }

    @Bean("applySpendingPeriodTemplate")
    fun applySpendingPeriodTemplate(
        spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
        spendingPeriodRepo: IRepository<SpendingPeriod>,
        profileRepo: IRepository<Profile>,
        foreCastSpending: IUseCase<ForcastSpendingPeriodInput, ForcastSpendingPeriodOutput>,
        eventRegister: IEventRegister,
        unitOfWork: IUnitOfWork,
    ): UseCase<Unit, BackgroundTaskOut> {
        return ApplySpendingPeriodTemplate(
            spendingPeriodTemplateRepo = spendingPeriodTemplateRepo,
            spendingPeriodRepo = spendingPeriodRepo,
            forecastSpendingPeriod = foreCastSpending,
            profileRepo = profileRepo,
            unitOfWork = unitOfWork,
            eventRegister = eventRegister,
        )
    }
}