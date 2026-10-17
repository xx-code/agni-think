package usecases.spending_period

import usecases.UseCase
import adapters.IAuthenticateAdapter
import adapters.repositories.IRepository
import domain.entities.Budget
import domain.entities.ScheduleInvoice
import domain.exceptions.NotFoundException
import domain.entities.SpendingPeriod
import domain.entities.SpendingPeriodTemplate
import domain.enums.InvoiceType
import domain.exceptions.ValidationException
import usecases.dto.CreatedOutput
import usecases.spending_period.dto.CreateSpendingPeriodInput
import domain.value_objects.Scheduler
import domain.value_objects.SnapshotForcastSpendingPeriod
import domain.value_objects.SpendingPeriodItem
import kotlin.collections.map

class CreateSpendingPeriod(
    private val spendingPeriodRepo: IRepository<SpendingPeriod>,
    private val spendingPeriodTemplateRepo: IRepository<SpendingPeriodTemplate>,
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    private val budgetRepo: IRepository<Budget>,
    auth: IAuthenticateAdapter
): UseCase<CreateSpendingPeriodInput, CreatedOutput>(auth = auth) {
    override suspend fun process(input: CreateSpendingPeriodInput): CreatedOutput {
        val template = spendingPeriodTemplateRepo.get(input.spendingPeriodTemplateId) ?: throw NotFoundException.SingleEntity(input.spendingPeriodTemplateId, "spending_period_template")

        val scheduler = Scheduler(
            template.startDate.atStartOfDay(),
            template.recurrence
        )

        if (input.scheduleIncomesSelected.isNotEmpty()) {
            val scheduleIncomes = scheduleInvoiceRepo.getManyByIds(input.scheduleIncomesSelected)
            if (input.scheduleIncomesSelected.size != scheduleIncomes.size)
                throw NotFoundException.EntitiesByOtherField(mapOf("ids" to input.scheduleIncomesSelected.joinToString()), "schedule_invoice")

            if (scheduleIncomes.any {it.invoiceType != InvoiceType.INCOME })
                throw ValidationException.SpendingPeriodIncomeMustBeOnlyScheduleIncome()
        }

        if (input.scheduleExpensesSelected.isNotEmpty()) {
            val scheduleExpenses = scheduleInvoiceRepo.getManyByIds(input.scheduleExpensesSelected)
            if (input.scheduleExpensesSelected.size != scheduleExpenses.size)
                throw NotFoundException.EntitiesByOtherField(mapOf("ids" to input.scheduleExpensesSelected.joinToString()), "schedule_invoice")

            if (scheduleExpenses.any {it.invoiceType == InvoiceType.INCOME })
                throw ValidationException.SpendingPeriodIncomeMustBeOnlyScheduleExpense()
        }

        if (input.budgetsSelected.isNotEmpty()) {
            if (input.budgetsSelected.size != budgetRepo.getManyByIds(input.budgetsSelected).size)
                throw NotFoundException.EntitiesByOtherField(mapOf("ids" to input.budgetsSelected.joinToString()), "budget")
        }

        val newSpendingPeriod = SpendingPeriod(
            userId = template.userId,
            spendingPeriodTemplateId = input.spendingPeriodTemplateId,
            startDate = template.startDate,
            endDate = scheduler.upgradeDate().toLocalDate(),
            savingRateTarget = input.savingRateTarget,
            state = input.state,
            scheduleIncomesSelected = input.scheduleIncomesSelected,
            scheduleExpensesSelected = input.scheduleExpensesSelected,
            budgetsSelected = input.budgetsSelected,
            wishlistItemSelected = input.wishlistItemSelected,
            snapshot = SnapshotForcastSpendingPeriod(
                income = 0.0,
                fixExpenses = 0.0,
                variableExpenses = 0.0,
                budgetExpenses = 0.0,
                saving = 0.0
            )
        )

        spendingPeriodRepo.create(newSpendingPeriod)

        return CreatedOutput(newSpendingPeriod.id)
    }
}
