package usecases.spending_period

import usecases.UseCase
import adapters.IAuthenticateAdapter
import adapters.repositories.IRepository
import domain.entities.Budget
import domain.entities.ScheduleInvoice
import domain.exceptions.NotFoundException
import domain.entities.SpendingPeriod
import domain.enums.InvoiceType
import domain.exceptions.ValidationException
import usecases.spending_period.dto.UpdateSpendingPeriodInput
import domain.value_objects.SpendingPeriodItem

class UpdateSpendingPeriod(
    private val spendingPeriodRepo: IRepository<SpendingPeriod>,
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    private val budgetRepo: IRepository<Budget>,
    auth: IAuthenticateAdapter
): UseCase<UpdateSpendingPeriodInput, Unit>(auth = auth) {
    override suspend fun process(input: UpdateSpendingPeriodInput) {
        val spendPeriod = spendingPeriodRepo.get(input.id) ?: throw NotFoundException.SingleEntity(input.id, "spending_period")

        input.startDate?.let { spendPeriod.startDate = it }
        input.endDate?.let { spendPeriod.endDate = it }
        input.savingRateTarget?.let { spendPeriod.savingRateTarget = it }
        input.state?.let { spendPeriod.state = it }
        input.scheduleIncomesSelected?.let {
            val scheduleIncomes = scheduleInvoiceRepo.getManyByIds(input.scheduleIncomesSelected)
            if (input.scheduleIncomesSelected.size != scheduleIncomes.size)
                throw NotFoundException.EntitiesByOtherField(mapOf("ids" to input.scheduleIncomesSelected.joinToString()), "schedule_invoice")

            if (scheduleIncomes.any {it.invoiceType != InvoiceType.INCOME })
                throw ValidationException.SpendingPeriodIncomeMustBeOnlyScheduleIncome()
            spendPeriod.scheduleIncomesSelected = it
        }

        input.scheduleExpensesSelected?.let {
            val scheduleExpenses = scheduleInvoiceRepo.getManyByIds(input.scheduleExpensesSelected)
            if (input.scheduleExpensesSelected.size != scheduleExpenses.size)
                throw NotFoundException.EntitiesByOtherField(mapOf("ids" to input.scheduleExpensesSelected.joinToString()), "schedule_invoice")

            if (scheduleExpenses.any {it.invoiceType == InvoiceType.INCOME })
                throw ValidationException.SpendingPeriodIncomeMustBeOnlyScheduleExpense()

            spendPeriod.scheduleExpensesSelected = it
        }

        input.budgetsSelected?.let {
            if (input.budgetsSelected.size != budgetRepo.getManyByIds(input.budgetsSelected).size)
                throw NotFoundException.EntitiesByOtherField(mapOf("ids" to input.budgetsSelected.joinToString()), "budget")
            spendPeriod.budgetsSelected = it
        }

        if (spendPeriod.hasChanged())
            spendingPeriodRepo.update(spendPeriod)
    }
}
