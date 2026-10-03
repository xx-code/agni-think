package usecases.spending_period_template

import usecases.UseCase
import adapters.dto.QueryFilter
import adapters.dto.ScheduleRepeaterOutput
import adapters.repositories.IRepository
import domain.entities.Budget
import domain.entities.SpendingPeriodTemplate
import usecases.dto.ListOutput
import usecases.spending_period_template.dto.GetSpendingPeriodTemplateBudgetOutput
import usecases.spending_period_template.dto.GetSpendingPeriodTemplateOutput

class GetAllSpendingPeriodTemplate(
    private val spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
    private val budgetRepo: IRepository<Budget>,
): UseCase<QueryFilter, ListOutput<GetSpendingPeriodTemplateOutput>>() {
    override suspend fun process(input: QueryFilter): ListOutput<GetSpendingPeriodTemplateOutput> {
        val spendingPeriodTemplate = spendingPeriodTemplateRepo.getAll(input)

        val budgets = budgetRepo.getManyByIds(spendingPeriodTemplate.items.flatMap { it.targetBudgetIds }.toSet())

        return ListOutput(
            items = spendingPeriodTemplate.items.map {
                GetSpendingPeriodTemplateOutput(
                    id = it.id,
                    recurrence = ScheduleRepeaterOutput(
                        period = it.recurrence.period.value,
                        interval = it.recurrence.interval,
                    ),
                    isActive = it.checkIsActive(),
                    startDate = it.startDate,
                    endDate = it.endDate,
                    budgets = budgets.filter { budget -> it.targetBudgetIds.contains(budget.id) }.map { budget ->
                        GetSpendingPeriodTemplateBudgetOutput(
                            id = budget.id,
                            title = budget.title,
                        )
                    }
                )
            },
            total = spendingPeriodTemplate.total
        )
    }
}