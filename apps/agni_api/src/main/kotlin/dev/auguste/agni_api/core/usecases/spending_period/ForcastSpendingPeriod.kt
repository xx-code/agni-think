package dev.auguste.agni_api.core.usecases.spending_period

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryComparator
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryDateComparator
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryScheduleInvoiceExtend
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.Budget
import dev.auguste.agni_api.core.entities.Profile
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.usecases.analystics.dto.ForcastSpendingInput
import dev.auguste.agni_api.core.usecases.analystics.dto.ForcastSpendingOutput
import dev.auguste.agni_api.core.usecases.budgets.dto.GetBudgetOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.GetInvoice
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetInvoiceOutput
import dev.auguste.agni_api.core.usecases.spending_period.dto.ForcastSpendingAchieveItemOutput
import dev.auguste.agni_api.core.usecases.spending_period.dto.ForcastSpendingPeriodInput
import dev.auguste.agni_api.core.usecases.spending_period.dto.ForcastSpendingPeriodOutput
import java.time.LocalDate
import java.util.UUID

class ForcastSpendingPeriod(
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    private val accountRepo: IRepository<Account>,
    private val budgetRepo: IRepository<Budget>,
    private val profileRepo: IRepository<Profile>,
    private val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
    private val getInvoice: IUseCase<GetInvoice, GetInvoiceOutput>,
): IUseCase<ForcastSpendingPeriodInput, ForcastSpendingPeriodOutput> {
    override fun execAsync(input: ForcastSpendingPeriodInput): ForcastSpendingPeriodOutput {
        val budgets = budgetRepo.getManyByIds(input.budgetIds.toSet())
        val accounts = accountRepo.getAll(QueryFilter.queryAll())
        val scheduleInvoices = scheduleInvoiceRepo.getAll(QueryFilter.queryAll(), QueryScheduleInvoiceExtend(
            comparatorDueDate = QueryDateComparator(
                input.endDate.atStartOfDay(),
                comparator = QueryComparator.LesserOrEquals,
            )
        ))

        val incomes = getIncomes(scheduleInvoices.items.filter{ it.scheduler.date.toLocalDate() >= input.startDate}, input.startDate, input.endDate)
        val fixExpenses = getFixExpenses(scheduleInvoices.items.filter {  it.scheduler.date.toLocalDate() >= input.startDate} , input.startDate, input.endDate)
        val variableExpenses = getVariableExpenses(scheduleInvoices.items.filter {  it.scheduler.date.toLocalDate() >= input.startDate} , input.startDate, input.endDate)

        val profiles = profileRepo.getAll(QueryFilter.queryAll())
        var savingRate = profiles.items.firstOrNull()?.savingPercentage ?: 0.0
        if (input.savingRate != null)
            savingRate = input.savingRate

        return ForcastSpendingPeriodOutput(

        )
    }

    fun getIncomes(scheduleInvoices: List<ScheduleInvoice>, startDate: LocalDate, endDate: LocalDate): List<ForcastSpendingAchieveItemOutput> {

    }

    fun getFixExpenses(scheduleInvoices: List<ScheduleInvoice>, startDate: LocalDate, endDate: LocalDate): List<ForcastSpendingAchieveItemOutput>  {

    }

    fun getVariableExpenses(scheduleInvoices: List<ScheduleInvoice>, startDate: LocalDate, endDate: LocalDate): List<ForcastSpendingAchieveItemOutput>  {

    }
}