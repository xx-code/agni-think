package usecases.analystics

import adapters.IAuthenticateAdapter
import adapters.readers.IGetAllCategoryReader
import usecases.UseCase
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Category
import domain.entities.User
import usecases.dto.ListOutput
import usecases.analystics.dto.GetSpendByCategoryInput
import usecases.analystics.dto.GetSpendByCategoryOutput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetBalanceByPeriodOutput
import usecases.invoices.dto.GetBalancesByPeriodInput

class GetSpendByCategoryAnalytic(
    private val categoryRepo: IGetAllCategoryReader,
    private val getBalanceByPeriod : IUseCase<GetBalancesByPeriodInput, List<GetBalanceByPeriodOutput>>,
    auth: IAuthenticateAdapter
): UseCase<GetSpendByCategoryInput, ListOutput<GetSpendByCategoryOutput>>(auth = auth) {

    override suspend fun process(input: GetSpendByCategoryInput): ListOutput<GetSpendByCategoryOutput> {
        val currentUser = getCurrentUser()

        val condition = QueryExtendBuilder<Category>()
            .addCondition("isSystem", QueryComparator.Equal, false)
        val categories = categoryRepo.read(input.query,  currentUser.id, condition)

        val result = mutableListOf<GetSpendByCategoryOutput>()
        for (category in categories.items) {
            val categoryBalances = getBalanceByPeriod.processDirect(GetBalancesByPeriodInput(
                userId = currentUser.id,
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