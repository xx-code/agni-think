package usecases.spending_period_template

import adapters.dto.ScheduleRepeaterOutput
import adapters.repositories.IRepository
import domain.entities.Budget
import domain.exceptions.NotFoundException
import domain.entities.SpendingPeriodTemplate
import usecases.interfaces.IUseCase
import usecases.spending_period_template.dto.GetSpendingPeriodTemplateBudgetOutput
import usecases.spending_period_template.dto.GetSpendingPeriodTemplateOutput
import java.util.UUID

class GetSpendingPeriodTemplate(
    private val spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
    private val budgetRepo: IRepository<Budget>,
): IUseCase<UUID, GetSpendingPeriodTemplateOutput>{
    override fun execAsync(input: UUID): GetSpendingPeriodTemplateOutput {
        val spendPeriodTemplate = spendingPeriodTemplateRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "spending_period_template")
        val budgets = budgetRepo.getManyByIds(spendPeriodTemplate.targetBudgetIds)
        return GetSpendingPeriodTemplateOutput(
            id = spendPeriodTemplate.id,
            recurrence = ScheduleRepeaterOutput(
                spendPeriodTemplate.recurrence.period.value,
                spendPeriodTemplate.recurrence.interval
            ),
            isActive = spendPeriodTemplate.checkIsActive(),
            startDate = spendPeriodTemplate.startDate,
            endDate = spendPeriodTemplate.endDate,
            budgets = budgets.map { it ->
                GetSpendingPeriodTemplateBudgetOutput(
                    id = it.id,
                    title = it.title
                )
            }
        )
    }
}