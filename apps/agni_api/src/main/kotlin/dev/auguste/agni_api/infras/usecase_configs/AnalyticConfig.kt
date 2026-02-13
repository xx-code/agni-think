package dev.auguste.agni_api.infras.usecase_configs

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.Category
import dev.auguste.agni_api.core.entities.Tag
import dev.auguste.agni_api.core.usecases.analystics.GetSavingAnalytic
import dev.auguste.agni_api.core.usecases.analystics.GetSpendAnalytic
import dev.auguste.agni_api.core.usecases.analystics.dto.GetAnalysticInput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSavingAnalyticOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSpendAnalyticOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalancesByPeriodInput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class AnalyticConfig {
    
    @Bean
    fun getSpendAnalytic(
        categoryRepo: IRepository<Category>,
        tagRepo: IRepository<Tag>,
        getBalanceByPeriod: IUseCase<GetBalancesByPeriodInput, List<GetBalanceOutput>>,
    ) : IUseCase<GetAnalysticInput, GetSpendAnalyticOutput> {
        return GetSpendAnalytic(
            categoryRepo = categoryRepo,
            tagRepo = tagRepo,
            getBalanceByPeriod = getBalanceByPeriod
        )
    }

    @Bean
    fun getSavingAnalytic(
        accountRepo: IRepository<Account>,
        getBalanceByPeriod: IUseCase<GetBalancesByPeriodInput, List<GetBalanceOutput>>,
    ) : IUseCase<GetAnalysticInput, GetSavingAnalyticOutput> {
        return GetSavingAnalytic(
            accountRepo = accountRepo,
            getBalanceByPeriod = getBalanceByPeriod
        )
    }
}