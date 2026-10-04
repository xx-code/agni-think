package usecases.analystics

import usecases.UseCase
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Category
import domain.entities.ScheduleInvoice
import domain.enums.InvoiceType
import domain.enums.PeriodType
import usecases.dto.ListOutput
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
    private val getBudgets: IUseCase<GetAllBudgetInput, ListOutput<GetBudgetOutput>>,
    private val getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
    private val getSavingBalance: IUseCase<GetSavingBalanceInput, Double>,
): UseCase<Unit, GetAnnualOutlookOutput>() {
    override suspend fun process(input: Unit): GetAnnualOutlookOutput {
        val conditionScheduleInvoice = QueryExtendBuilder<ScheduleInvoice>()
            .addCondition("scheduler.date", QueryComparator.Greater, LocalDateTime.now())

        val scheduleInvoices = scheduleRepo.getAll(QueryFilter(0, 0, true), conditionScheduleInvoice)
        val currentDateTime = LocalDateTime.now()
        val currentBalance = getBalance.processDirect(GetBalanceInput(
            startDate = currentDateTime.with(TemporalAdjusters.firstDayOfYear()),
            endDate = currentDateTime
        ))

        val nextIncome = getFutureOutlook(scheduleInvoices.items.filter { it.type == InvoiceType.INCOME })
        val incomeOutlook = nextIncome + currentBalance.income

        val nextSpend = getFutureOutlook(scheduleInvoices.items.filter { it.type != InvoiceType.INCOME && !it.isPause && !it.isFreeze })
        val (currentBudgetOutlook, targetBudgetOutlook) = getBudgetBalances()
        val remindBudget = if (currentBudgetOutlook > targetBudgetOutlook) 0.0 else (targetBudgetOutlook - currentBudgetOutlook)

        val spendOutlook = nextSpend + remindBudget + currentBalance.spend

        val savingMargin = incomeOutlook - spendOutlook
        val currentSaving = getSavingBalance.processDirect(GetSavingBalanceInput(
            startDate = currentDateTime.with(TemporalAdjusters.firstDayOfYear()),
            endDate = currentDateTime
        ))

        val conditionCategory = QueryExtendBuilder<Category>()
            .addCondition("isSystem", QueryComparator.Equal, false)
        val categories = categoryRepo.getAll(QueryFilter(0, 0, true), conditionCategory)
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


    private suspend fun getBudgetBalances(): Pair<Double, Double> {
        val budgets = getBudgets.processDirect(GetAllBudgetInput(query = QueryFilter.queryAll())).items


        val now = LocalDateTime.now()
        val totalCurrentAmount = getBalance.processDirect(GetBalanceInput(
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
                val now = it.scheduler.date.toLocalDate()
                val end = now.with(TemporalAdjusters.lastDayOfYear())
                it.amount * it.scheduler.repeater!!.computeOccurrences(now, end)
            }
        }
    }

    private suspend fun getCurrentBalanceByCategory(categories: List<Category>): List<SpendByCategoryOutlook> {
        val spends = mutableListOf<SpendByCategoryOutlook>()
        val endDate = LocalDateTime.now()
        val startDate = endDate.with(TemporalAdjusters.firstDayOfYear())

        // TODO: Optimization multiple call here
        categories.forEach {
            val balance = getBalance.processDirect(GetBalanceInput(
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