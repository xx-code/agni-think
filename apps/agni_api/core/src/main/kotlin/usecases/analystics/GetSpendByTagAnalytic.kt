package usecases.analystics

import usecases.UseCase
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Tag
import usecases.dto.ListOutput
import usecases.analystics.dto.GetSpendByTagInput
import usecases.analystics.dto.GetSpendByTagOutput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetBalanceByPeriodOutput
import usecases.invoices.dto.GetBalancesByPeriodInput

class GetSpendByTagAnalytic(
    private val tagRepo: IRepository<Tag>,
    private val getBalanceByPeriod : IUseCase<GetBalancesByPeriodInput, List<GetBalanceByPeriodOutput>>
): UseCase<GetSpendByTagInput, ListOutput<GetSpendByTagOutput>>() {

    override suspend fun process(input: GetSpendByTagInput): ListOutput<GetSpendByTagOutput> {
        val condition = QueryExtendBuilder<Tag>()
            .addCondition("is_system", QueryComparator.Equal, false)
        val tags = tagRepo.getAll(input.query, condition)

        val result = mutableListOf<GetSpendByTagOutput>()
        for (tag in tags.items) {
            val tagBalances = getBalanceByPeriod.processDirect(GetBalancesByPeriodInput(
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