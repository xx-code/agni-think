package dev.auguste.agni_api.core.usecases.analystics

import dev.auguste.agni_api.core.SAVING_CATEGORY_ID
import dev.auguste.agni_api.core.TRANSFERT_CATEGORY_ID
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Category
import dev.auguste.agni_api.core.entities.Tag
import dev.auguste.agni_api.core.usecases.analystics.dto.GetAnalysticInput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSpendAnalyticOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.SpendByCategoryOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.SpendByTagOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalancesByPeriodInput

class GetSpendAnalytic(
    private val categoryRepo: IRepository<Category>,
    private val tagRepo: IRepository<Tag>,
    private val getBalanceByPeriod : IUseCase<GetBalancesByPeriodInput, List<GetBalanceOutput>>
) : IUseCase<GetAnalysticInput, GetSpendAnalyticOutput> {

    override fun execAsync(input: GetAnalysticInput): GetSpendAnalyticOutput {
        val categories = categoryRepo.getAll(QueryFilter(0, 0, true))
        val tags = tagRepo.getAll(QueryFilter(0, 0, true))

        return GetSpendAnalyticOutput(
            totalSpend = listOf(),
            spendByCategories = listOf(),
        )

    }
}