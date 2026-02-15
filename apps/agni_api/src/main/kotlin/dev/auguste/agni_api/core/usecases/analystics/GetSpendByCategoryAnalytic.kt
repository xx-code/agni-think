package dev.auguste.agni_api.core.usecases.analystics

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryCategoryExtend
import dev.auguste.agni_api.core.entities.Category
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSpendByCategoryInput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSpendByCategoryOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalancesByPeriodInput

class GetSpendByCategoryAnalytic(
    private val categoryRepo: IRepository<Category>,
    private val getBalanceByPeriod : IUseCase<GetBalancesByPeriodInput, List<GetBalanceOutput>>
) : IUseCase<GetSpendByCategoryInput, ListOutput<GetSpendByCategoryOutput>> {

    override fun execAsync(input: GetSpendByCategoryInput): ListOutput<GetSpendByCategoryOutput> {
        val categories = categoryRepo.getAll(input.query, QueryCategoryExtend(false))

        val result = mutableListOf<GetSpendByCategoryOutput>()
        for (category in categories.items) {
            val categoryBalances = getBalanceByPeriod.execAsync(GetBalancesByPeriodInput(
                period = input.period,
                interval = input.interval,
                dateFrom = input.startDate,
                categoryIds = setOf(category.id)
            ))

            result.add(
                GetSpendByCategoryOutput(
                    categoryId = category.id,
                    spends = categoryBalances.map { it.spend },
                ))
        }

        return ListOutput(
            items = result,
            total = categories.total
        )
    }
}