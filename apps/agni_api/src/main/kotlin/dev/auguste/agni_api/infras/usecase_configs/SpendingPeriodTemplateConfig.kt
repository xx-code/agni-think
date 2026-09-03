package dev.auguste.agni_api.infras.usecase_configs

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Budget

import dev.auguste.agni_api.core.entities.SpendingPeriodTemplate
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
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
}