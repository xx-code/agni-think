package dev.auguste.agni_api.infras.usecase_configs

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.events.IEventRegister
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.entities.SpendingPeriod

import dev.auguste.agni_api.core.entities.SpendingPeriodTemplate
import dev.auguste.agni_api.core.usecases.BackgroundTaskOut
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.ForcastSpendingInput
import dev.auguste.agni_api.core.usecases.analystics.dto.ForcastSpendingOutput
import dev.auguste.agni_api.core.usecases.interfaces.ISuspendableUseCase
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.spending_period.dto.GetAllSpendingPeriodInput
import dev.auguste.agni_api.core.usecases.spending_period_template.ApplySpendingPeriodTemplate
import dev.auguste.agni_api.core.usecases.spending_period_template.CreateSpendingPeriodTemplate
import dev.auguste.agni_api.core.usecases.spending_period_template.DeleteSpendingPeriodTemplate
import dev.auguste.agni_api.core.usecases.spending_period_template.GetAllSpendingPeriodTemplate
import dev.auguste.agni_api.core.usecases.spending_period_template.GetSpendingPeriodTemplate
import dev.auguste.agni_api.core.usecases.spending_period_template.UpdateSpendingPeriodTemplate
import dev.auguste.agni_api.core.usecases.spending_period_template.dto.CreateSpendingPeriodTemplateInput
import dev.auguste.agni_api.core.usecases.spending_period_template.dto.GetSpendingPeriodTemplateOutput
import dev.auguste.agni_api.core.usecases.spending_period_template.dto.UpdateSpendingPeriodTemplateInput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class SpendingPeriodTemplateConfig {

    @Bean
    fun createSpendingPeriodTemplate(
        spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
        budgetRepo: IRepository<Budget>
    ): IUseCase<CreateSpendingPeriodTemplateInput, CreatedOutput> {
        return CreateSpendingPeriodTemplate(
            spendingPeriodTemplateRepo = spendingPeriodTemplateRepo,
            budgetRepo = budgetRepo,
        )
    }

    @Bean
    fun updateSpendingPeriodTemplate(
        spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
        budgetRepo: IRepository<Budget>
    ): IUseCase<UpdateSpendingPeriodTemplateInput, Unit> {
        return UpdateSpendingPeriodTemplate(
            spendingPeriodTemplateRepo = spendingPeriodTemplateRepo,
            budgetRepo = budgetRepo,
        )
    }

    @Bean
    fun getSpendingPeriodTemplate(
        spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
        budgetRepo: IRepository<Budget>
    ): IUseCase<UUID, GetSpendingPeriodTemplateOutput> {
        return GetSpendingPeriodTemplate(
            spendingPeriodTemplateRepo = spendingPeriodTemplateRepo,
            budgetRepo = budgetRepo
        )
    }

    @Bean
    fun getAllSpendingPeriodTemplate(
        spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
        budgetRepo: IRepository<Budget>
    ): IUseCase<QueryFilter, ListOutput<GetSpendingPeriodTemplateOutput>> {
        return GetAllSpendingPeriodTemplate(
            spendingPeriodTemplateRepo = spendingPeriodTemplateRepo,
            budgetRepo = budgetRepo,
        )
    }

    @Bean("deleteSpendingPeriodTemplate")
    fun deleteSpendingPeriodTemplate(
        spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
    ) : IUseCase<UUID, Unit> {
       return DeleteSpendingPeriodTemplate(
           spendingPeriodTemplateRepo = spendingPeriodTemplateRepo
       )
    }

    @Bean("applySpendingPeriodTemplate")
    fun applySpendingPeriodTemplate(
        spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
        spendingPeriodRepo: IRepository<SpendingPeriod>,
        foreCastSpending: IUseCase<ForcastSpendingInput, ForcastSpendingOutput>,
        eventRegister: IEventRegister,
        unitOfWork: IUnitOfWork,
    ): ISuspendableUseCase<Unit, BackgroundTaskOut> {
        return ApplySpendingPeriodTemplate(
            spendingPeriodTemplateRepo = spendingPeriodTemplateRepo,
            spendingPeriodRepo = spendingPeriodRepo,
            forecastSpendingPeriod = foreCastSpending,
            unitOfWork = unitOfWork,
            eventRegister = eventRegister
        )
    }
}