package usecases.analystics

import usecases.UseCase
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Category
import usecases.dto.ListOutput
import usecases.analystics.dto.GetSpendByCategoryInput
import usecases.analystics.dto.GetSpendByCategoryOutput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetBalanceByPeriodOutput
import usecases.invoices.dto.GetBalancesByPeriodInput

class GetSpendByCategoryAnalytic(
    private val categoryRepo: IRepository<Category>,
    private val getBalanceByPeriod : IUseCase<GetBalancesByPeriodInput, List<GetBalanceByPeriodOutput>>
): UseCase<GetSpendByCategoryInput, ListOutput<GetSpendByCategoryOutput>>() {

    override suspend fun process(input: GetSpendByCategoryInput): ListOutput<GetSpendByCategoryOutput> {
        val condition = QueryExtendBuilder<Category>()
            .addCondition("isSystem", QueryComparator.Equal, false)
        val categories = categoryRepo.getAll(input.query, condition)

        val result = mutableListOf<GetSpendByCategoryOutput>()
        for (category in categories.items) {
            val categoryBalances = getBalanceByPeriod.processDirect(GetBalancesByPeriodInput(
                period = input.period,
                interval = input.interval,
                dateFrom = input.startDate,
                categoryIds = setOf(category.id)
            ))

            result.add(
                GetSpendByCategoryOutput(
                    categoryId = category.id,
                    icon = category.icon,
                    title = category.title,
                    color = category.color.toString(),
                    spends = categoryBalances.map { it.spend },
                )
            )
        }

        return ListOutput(
            items = result,
            total = categories.total
        )
    }
}