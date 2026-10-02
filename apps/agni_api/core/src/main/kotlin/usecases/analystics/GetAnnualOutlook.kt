package usecases.analystics

import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.query_extend.QueryComparator
import adapters.repositories.query_extend.QueryCategoryExtend
import adapters.repositories.query_extend.QueryDateComparator
import adapters.repositories.query_extend.QueryScheduleInvoiceExtend
import domain.entities.Category
import domain.entities.ScheduleInvoice
import domain.enums.InvoiceType
import domain.enums.PeriodType
import usecases.ListOutput
import usecases.analystics.dto.GetAnnualOutlookOutput
import usecases.analystics.dto.GetSavingBalanceInput
import usecases.analystics.dto.SpendByCategoryOutlook
import usecases.budgets.dto.GetAllBudgetInput
import usecases.budgets.dto.GetBudgetOutput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetBalanceInput
import usecases.invoices.dto.GetBalanceOutput
import domain.value_objects.SchedulerRecurrence
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import kotlin.collections.filter

class GetAnnualOutlook(
    private val scheduleRepo: IRepository<ScheduleInvoice>,
    private val categoryRepo: IRepository<Category>,
    private val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
    private val getBudgets: IUseCase<GetAllBudgetInput, ListOutput<GetBudgetOutput>>,
    private val getSavingBalance: IUseCase<GetSavingBalanceInput, Double>,
): IUseCase<Unit, GetAnnualOutlookOutput> {
    override fun execAsync(input: Unit): GetAnnualOutlookOutput {
        val scheduleInvoices = scheduleRepo.getAll(
            QueryFilter(0, 0, true),
            QueryScheduleInvoiceExtend(
                QueryDateComparator(LocalDateTime.now(), comparator = QueryComparator.Greater)),
        )
        val currentDateTime = LocalDateTime.now()
        val currentBalance = getBalance.execAsync(GetBalanceInput(
            startDate = currentDateTime.with(TemporalAdjusters.firstDayOfYear()),
            status = null, // Take even pending
            endDate = currentDateTime
        ))

        val nextIncome = getFutureOutlook(scheduleInvoices.items.filter { it.type == _root_ide_package_.domain.enums.InvoiceType.INCOME })
        val incomeOutlook = nextIncome + currentBalance.income

        val nextSpend = getFutureOutlook(scheduleInvoices.items.filter { it.type != _root_ide_package_.domain.enums.InvoiceType.INCOME && !it.isPause && !it.isFreeze })
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
        val budgets = getBudgets.execAsync(GetAllBudgetInput(query = QueryFilter.queryAll())).items


        val now = LocalDateTime.now()
        val totalCurrentAmount = getBalance.execAsync(GetBalanceInput(
            startDate = now.with(TemporalAdjusters.firstDayOfYear()),
            status = null,
            endDate = now,
            budgetIds = budgets.map { it.id }.toSet()
        )).spend

        val totalTargetAmount = budgets.sumOf {
            // if there are no repeater it's a year compute
            val period = _root_ide_package_.domain.enums.PeriodType.fromString(it.repeater?.period ?: "YEAR")
            if (period == _root_ide_package_.domain.enums.PeriodType.YEAR)
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
                val now = it.scheduler.date.toLocalDate()
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
        spendByCategories.filter { scheduleInvoice.map { sch -> sch.categoryId }.contains(it.categoryId) }.forEach { spentCategory ->
            val futureSpend = getFutureOutlook(scheduleInvoice.filter { it.categoryId == spentCategory.categoryId })

            futureSpends.add(SpendByCategoryOutlook(
                categoryId = spentCategory.categoryId,
                spend = spentCategory.spend + futureSpend
            ))
        }

        return futureSpends
    }
}