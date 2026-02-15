package dev.auguste.agni_api.core.usecases.analystics

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryTagExtend
import dev.auguste.agni_api.core.entities.Tag
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSpendByTagInput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSpendByTagOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalancesByPeriodInput

class GetSpendByTagAnalytic(
    private val tagRepo: IRepository<Tag>,
    private val getBalanceByPeriod : IUseCase<GetBalancesByPeriodInput, List<GetBalanceOutput>>
) : IUseCase<GetSpendByTagInput, ListOutput<GetSpendByTagOutput>> {

    override fun execAsync(input: GetSpendByTagInput): ListOutput<GetSpendByTagOutput> {
        val tags = tagRepo.getAll(input.query, QueryTagExtend(false))

        val result = mutableListOf<GetSpendByTagOutput>()
        for (tag in tags.items) {
            val tagBalances = getBalanceByPeriod.execAsync(GetBalancesByPeriodInput(
                period = input.period,
                interval = input.interval,
                dateFrom = input.startDate,
                categoryIds = input.categoryId?.let { setOf(it) }
            ))

            result.add(
                GetSpendByTagOutput(
                    tagId = tag.id,
                    spends = tagBalances.map { it.spend },
                ))
        }

        return ListOutput(
            items = result,
            total = tags.total
        )
    }
}