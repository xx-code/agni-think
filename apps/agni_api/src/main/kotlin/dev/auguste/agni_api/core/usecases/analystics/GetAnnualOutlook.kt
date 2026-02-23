package dev.auguste.agni_api.core.usecases.analystics

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.query_extend.ComparatorType
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryCategoryExtend
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryDateComparator
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryScheduleInvoiceExtend
import dev.auguste.agni_api.core.entities.Category
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.entities.enums.PeriodType
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetAnnualOutlookOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSavingBalanceInput
import dev.auguste.agni_api.core.usecases.analystics.dto.SpendByCategoryOutlook
import dev.auguste.agni_api.core.usecases.budgets.dto.GetBudgetOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import dev.auguste.agni_api.core.value_objects.SchedulerRecurrence
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import kotlin.collections.filter

class GetAnnualOutlook(
    private val scheduleRepo: IRepository<ScheduleInvoice>,
    private val categoryRepo: IRepository<Category>,
    private val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
    private val getBudgets: IUseCase<QueryFilter, ListOutput<GetBudgetOutput>>,
    private val getSavingBalance: IUseCase<GetSavingBalanceInput, Double>,
): IUseCase<Unit, GetAnnualOutlookOutput> {
    override fun execAsync(input: Unit): GetAnnualOutlookOutput {
        val scheduleInvoices = scheduleRepo.getAll(
            QueryFilter(0, 0, true),
            QueryScheduleInvoiceExtend(
                QueryDateComparator(LocalDateTime.now(), ComparatorType.Greater)),
        )
        val currentDateTime = LocalDateTime.now()
        val currentBalance = getBalance.execAsync(GetBalanceInput(
            startDate = currentDateTime.with(TemporalAdjusters.firstDayOfYear()),
            status = null, // Take even pending
            endDate = currentDateTime
        ))

        val nextIncome = getFutureOutlook(scheduleInvoices.items.filter { it.type == InvoiceType.INCOME })
        val incomeOutlook = nextIncome + currentBalance.income

        val nextSpend = getFutureOutlook(scheduleInvoices.items.filter { it.type != InvoiceType.INCOME && !it.isPause && !it.isFreeze })
        val (currentBudgetOutlook, targetBudgetOutlook) = getBudgetBalances()
        val remindBudget = if (currentBudgetOutlook > targetBudgetOutlook) 0.0 else (targetBudgetOutlook - currentBudgetOutlook)

        val spendOutlook = nextSpend + remindBudget + currentBalance.spend

        val savingMargin = incomeOutlook - spendOutlook
        val currentSaving = getSavingBalance.execAsync(GetSavingBalanceInput(
            startDate = currentDateTime.with(TemporalAdjusters.firstDayOfYear()),
            endDate = currentDateTime
        ))

        val categories = categoryRepo.getAll(QueryFilter(0, 0, true), QueryCategoryExtend(isSystem = false))
        val currentSpendByCategories = getCurrentBalanceByCategory(categories.items)
        val spendByCategoryOutlook = addFutureSpendByCategory(currentSpendByCategories, scheduleInvoices.items)

        return GetAnnualOutlookOutput(
            incomeOutlook = incomeOutlook,
            spendOutlook = spendOutlook,
            budgetOutlook = targetBudgetOutlook,
            savingMargin = savingMargin,
            currentIncomeOutlook = currentBalance.income,
            currentSpendOutlook = currentBalance.spend,
            currentBudgetOutlook = currentBudgetOutlook,
            currentSaving = currentSaving,
            spendByCategoriesOutlook = spendByCategoryOutlook,
            currentSpendByCategoryOutlook = currentSpendByCategories
        )
    }


    private fun getBudgetBalances(): Pair<Double, Double> {
        val budgets = getBudgets.execAsync(QueryFilter(
            0, 0, true
        )).items


        val now = LocalDateTime.now()
        val totalCurrentAmount = getBalance.execAsync(GetBalanceInput(
            startDate = now.with(TemporalAdjusters.firstDayOfYear()),
            status = null,
            endDate = now,
            budgetIds = budgets.map { it.id }.toSet()
        )).spend

        val totalTargetAmount = budgets.sumOf {
            // if there are no repeater it's a year compute
            val period = PeriodType.fromString(it.repeater?.period ?: "YEAR")
            if (period == PeriodType.YEAR)
                it.target
            else {
                val now = LocalDate.now()
                val schedulerRepeater = SchedulerRecurrence(period, it.repeater?.interval ?: 1)
                val end = now.with(TemporalAdjusters.lastDayOfYear())
                it.target * schedulerRepeater.computeOccurrences(now, end)
            }
        }

        return Pair(totalCurrentAmount, totalTargetAmount)
    }

    private fun getFutureOutlook(scheduleInvoices: List<ScheduleInvoice>): Double {
        return scheduleInvoices.sumOf {
            if (it.scheduler.repeater == null)
                it.amount
            else  {
                val now = LocalDate.now()
                val end = now.with(TemporalAdjusters.lastDayOfYear())
                it.amount * it.scheduler.repeater!!.computeOccurrences(now, end)
            }
        }
    }

    private fun getCurrentBalanceByCategory(categories: List<Category>): List<SpendByCategoryOutlook> {
        val spends = mutableListOf<SpendByCategoryOutlook>()
        val endDate = LocalDateTime.now()
        val startDate = endDate.with(TemporalAdjusters.firstDayOfYear())

        // TODO: Optimization multiple call here
        categories.forEach {
            val balance = getBalance.execAsync(GetBalanceInput(
                startDate = startDate,
                endDate = endDate,
                status = null,
                categoryIds = setOf(it.id)
            ))

            spends.add(SpendByCategoryOutlook(
                categoryId = it.id,
                spend = balance.spend
            ))
        }

        return spends
    }

    private fun addFutureSpendByCategory(spendByCategories: List<SpendByCategoryOutlook>, scheduleInvoice: List<ScheduleInvoice>): List<SpendByCategoryOutlook> {
        val futureSpends = mutableListOf<SpendByCategoryOutlook>()

        // TODO: Optimization multiple call here
        spendByCategories.forEach { spentCategory ->
            val futureSpend = getFutureOutlook(scheduleInvoice.filter { it.categoryId == spentCategory.categoryId })

            futureSpends.add(SpendByCategoryOutlook(
                categoryId = spentCategory.categoryId,
                spend = spentCategory.spend + futureSpend
            ))
        }

        return futureSpends
    }
}