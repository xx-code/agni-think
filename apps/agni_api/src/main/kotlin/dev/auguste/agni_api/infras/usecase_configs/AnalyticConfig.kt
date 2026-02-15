package dev.auguste.agni_api.infras.usecase_configs

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.Category
import dev.auguste.agni_api.core.entities.Tag
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.analystics.GetSavingAnalytic
import dev.auguste.agni_api.core.usecases.analystics.GetSpendByCategoryAnalytic
import dev.auguste.agni_api.core.usecases.analystics.GetSpendByTagAnalytic
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSavingAnalyticInput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSavingAnalyticOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSpendByCategoryInput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSpendByCategoryOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSpendByTagInput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSpendByTagOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalancesByPeriodInput
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class AnalyticConfig {
    
    @Bean
    fun getSpendCategoryAnalytic(
        categoryRepo: IRepository<Category>,
        getBalanceByPeriod: IUseCase<GetBalancesByPeriodInput, List<GetBalanceOutput>>,
    ) : IUseCase<GetSpendByCategoryInput, ListOutput<GetSpendByCategoryOutput>> {
        return GetSpendByCategoryAnalytic(
            categoryRepo = categoryRepo,
            getBalanceByPeriod = getBalanceByPeriod
        )
    }

    @Bean
    fun getSpendTagAnalytic(
        tagRepo: IRepository<Tag>,
        getBalanceByPeriod: IUseCase<GetBalancesByPeriodInput, List<GetBalanceOutput>>,
    ) : IUseCase<GetSpendByTagInput, ListOutput<GetSpendByTagOutput>> {
        return GetSpendByTagAnalytic(
            tagRepo = tagRepo,
            getBalanceByPeriod = getBalanceByPeriod
        )
    }

    @Bean
    fun getSavingAnalytic(
        accountRepo: IRepository<Account>,
        getBalanceByPeriod: IUseCase<GetBalancesByPeriodInput, List<GetBalanceOutput>>,
    ) : IUseCase<GetSavingAnalyticInput, GetSavingAnalyticOutput> {
        return GetSavingAnalytic(
            accountRepo = accountRepo,
            getBalanceByPeriod = getBalanceByPeriod
        )
    }
}