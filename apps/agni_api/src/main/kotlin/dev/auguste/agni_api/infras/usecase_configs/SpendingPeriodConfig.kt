package dev.auguste.agni_api.infras.usecase_configs

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.SpendingPeriod
import dev.auguste.agni_api.core.entities.SpendingPeriodTemplate
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.spending_period.CreateSpendingPeriod
import dev.auguste.agni_api.core.usecases.spending_period.DeleteSpendingPeriod
import dev.auguste.agni_api.core.usecases.spending_period.GetAllSpendingPeriod
import dev.auguste.agni_api.core.usecases.spending_period.GetSpendingPeriod
import dev.auguste.agni_api.core.usecases.spending_period.UpdateSpendingPeriod
import dev.auguste.agni_api.core.usecases.spending_period.dto.CreateSpendingPeriodInput
import dev.auguste.agni_api.core.usecases.spending_period.dto.GetAllSpendingPeriodInput
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
        spendingPeriodRepo: IRepository<SpendingPeriod>
    ): IUseCase<UUID, GetSpendingPeriodOutput> {
        return GetSpendingPeriod(
            spendingPeriodRepo = spendingPeriodRepo
        )
    }

    @Bean
    fun getAllSpendingPeriod(
        spendingPeriodRepo: IRepository<SpendingPeriod>
    ): IUseCase<GetAllSpendingPeriodInput, ListOutput<GetSpendingPeriodOutput>> {
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
}
