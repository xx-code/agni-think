package dev.auguste.agni_api.infras.usecase_configs

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.IncomeSource
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.income_sources.CreateIncomeSource
import dev.auguste.agni_api.core.usecases.income_sources.DeleteIncomeSource
import dev.auguste.agni_api.core.usecases.income_sources.GetAllIncomeSource
import dev.auguste.agni_api.core.usecases.income_sources.GetIncomeSource
import dev.auguste.agni_api.core.usecases.income_sources.UpdateIncomeSource
import dev.auguste.agni_api.core.usecases.income_sources.dto.CreateIncomeSourceInput
import dev.auguste.agni_api.core.usecases.income_sources.dto.DeleteIncomeSourceInput
import dev.auguste.agni_api.core.usecases.income_sources.dto.GetIncomeSourceOutput
import dev.auguste.agni_api.core.usecases.income_sources.dto.UpdateIncomeSourceInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class IncomeSourceConfig {

    @Bean
    fun createIncomeSource(
        incomeSourceRepo: IRepository<IncomeSource>,
        accountRepo: IRepository<Account>,
    ) : IUseCase<CreateIncomeSourceInput, CreatedOutput> {
        return CreateIncomeSource(
            incomeSourceRepo,
            accountRepo
        )
    }

    @Bean
    fun updateIncomeSource(
        incomeSourceRepo: IRepository<IncomeSource>,
        accountRepo: IRepository<Account>,
    ) : IUseCase<UpdateIncomeSourceInput, Unit> {
        return UpdateIncomeSource(
            incomeSourceRepo,
            accountRepo
        )
    }

    @Bean
    fun deleteIncomeSource(
        incomeSourceRepo: IRepository<IncomeSource>,
    ) : IUseCase<DeleteIncomeSourceInput, Unit> {
        return DeleteIncomeSource(
            incomeSourceRepo
        )
    }

    @Bean
    fun getIncomeSource(
        incomeSourceRepo: IRepository<IncomeSource>,
    ) : IUseCase<UUID, GetIncomeSourceOutput> {
        return GetIncomeSource(
            incomeSourceRepo
        )
    }

    @Bean
    fun getAllIncomeSource(
        incomeSourceRepo: IRepository<IncomeSource>,
    ) : IUseCase<QueryFilter, ListOutput<GetIncomeSourceOutput>> {
        return GetAllIncomeSource(
            incomeSourceRepo
        )
    }
}