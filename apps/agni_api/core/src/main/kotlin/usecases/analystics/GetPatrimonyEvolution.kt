package usecases.analystics

import adapters.IAuthenticateAdapter
import usecases.UseCase
import domain.SAVING_CATEGORY_ID
import adapters.dto.QueryFilter
import adapters.dto.QuerySortBy
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import adapters.repositories.QueryCondition
import domain.entities.Account
import domain.entities.Patrimony
import domain.entities.PatrimonySnapshot
import domain.entities.Fund
import domain.entities.User
import usecases.analystics.dto.GetPatrimonyEvolutionInput
import usecases.analystics.dto.GetPatrimonyEvolutionOutput
import usecases.analystics.dto.NetWorthPeriodOutput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetBalanceByPeriodOutput
import usecases.invoices.dto.GetBalancesByPeriodInput
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import java.util.UUID
import kotlin.math.abs
import domain.enums.InvoiceStatusType
import domain.enums.PatrimonyType
import domain.enums.PeriodType


class GetPatrimonyEvolution(
    private val patrimonyRepo: IRepository<Patrimony>,
    private val patrimonySnapshotRepo: IRepository<PatrimonySnapshot>,
    private val fundRepo: IRepository<Fund>,
    private val getBalanceByPeriod: IUseCase<GetBalancesByPeriodInput, List<GetBalanceByPeriodOutput>>,
    auth: IAuthenticateAdapter
): UseCase<GetPatrimonyEvolutionInput, GetPatrimonyEvolutionOutput>(auth = auth) {

    override suspend fun process(input: GetPatrimonyEvolutionInput): GetPatrimonyEvolutionOutput {
        val currentUser = getCurrentUser()

        val condition = QueryExtendBuilder<Patrimony>()
            .addCondition("userId", QueryComparator.Equal, currentUser.id)
        val patrimonies = patrimonyRepo.getAll(QueryFilter.queryAll(), condition)
        val patrimonyIds = patrimonies.items.map { it.id }.toSet()

        val conditionSnapshot = QueryExtendBuilder<PatrimonySnapshot>()
            .addCondition("patrimonyId", QueryComparator.In, patrimonyIds)

        // 1. Fetch all relevant snapshots sorted chronologically
        val snapshots = patrimonySnapshotRepo.getAll(
            QueryFilter(0, 0, true, QuerySortBy("date")),
            conditionSnapshot
        ).items

        // 2. Build target period interval buckets (e.g., last N months)
        val date = LocalDateTime.now().minusMonths(input.interval.toLong()).with(TemporalAdjusters.firstDayOfMonth())
        val periodBuckets = generatePeriodBuckets(date.toLocalDate(), input.periodType, input.interval)

        // 3. Compute evolution for each Patrimony
        val patrimonyAssetBreakdown = mutableMapOf<UUID, List<NetWorthPeriodOutput>>()
        val patrimonyLiabilityBreakdown = mutableMapOf<UUID, List<NetWorthPeriodOutput>>()

        for (patrimony in patrimonies.items) {
            val patrimonySnapshots = snapshots.filter { it.patrimonyId == patrimony.id }

            // Get balance history calculated from transactions if needed for fallback
            val balancesByPeriod = getBalanceByPeriod.processDirect(
                GetBalancesByPeriodInput(
                    userId = currentUser.id,
                    period = input.periodType,
                    interval = input.interval,
                    dateFrom = date,
                    accountIds = patrimony.accountIds.toSet(),
                    status = InvoiceStatusType.COMPLETED
                )
            ).associateBy { it.date }

            var lastKnownBalance = 0.0

            val evolutionPoints = periodBuckets.map { bucketDate ->
                // Find snapshots that occurred on or before this period bucket
                val snapshotInBucket = patrimonySnapshots
                    .filter { !it.date.isAfter(bucketDate) }
                    .maxByOrNull { it.date }

                val resolvedBalance = when {
                    // Scenario A: Precise snapshot found for this period or prior period
                    snapshotInBucket != null -> snapshotInBucket.currentBalanceObserved

                    // Scenario B: Fall back to calculated transaction-based balance for the date
                    balancesByPeriod.containsKey(bucketDate) -> patrimony.amount + balancesByPeriod[bucketDate]!!.balance

                    // Scenario C: Carry forward the last known recorded balance
                    else -> lastKnownBalance
                }

                lastKnownBalance = resolvedBalance // Update state for next interval iteration

                NetWorthPeriodOutput(
                    date = bucketDate,
                    networth = resolvedBalance
                )
            }
            if (patrimony.type == PatrimonyType.LIABILITY)
                patrimonyLiabilityBreakdown[patrimony.id] = evolutionPoints
            else
                patrimonyAssetBreakdown[patrimony.id] = evolutionPoints
        }

        // 4. Get actual total balance today
        val fundCondition = QueryExtendBuilder<Fund>()
            .addCondition("userId", QueryComparator.Equal, currentUser.id)
        val currentSavingGoalBalance = fundRepo.getAll(QueryFilter.queryAll(), fundCondition).items.sumOf { it.balance }

        // Fetch periodic savings activity
        val savingBalancesByDate = getBalanceByPeriod.processDirect(
            GetBalancesByPeriodInput(
                userId = currentUser.id,
                period = input.periodType,
                interval = input.interval,
                dateFrom = date,
                categoryIds = setOf(SAVING_CATEGORY_ID),
                status = InvoiceStatusType.COMPLETED
            )
        ).associateBy { it.date }

        // Compute total change from start date to current date
        var cumulativeBalance = currentSavingGoalBalance

        // Build period-by-period accumulated balance list
        val savingGoalBalancesPerPeriod = periodBuckets.sortedByDescending { it }.map { bucketDate ->
            val savingIn = savingBalancesByDate[bucketDate]?.spend ?: 0.0
            val savingOut = savingBalancesByDate[bucketDate]?.income ?: 0.0

            val delta = savingIn - savingOut

            cumulativeBalance -= delta
            bucketDate to cumulativeBalance
        }.toMap()

        // 5. Aggregate overall net worth per period
        val aggregatedTotals = periodBuckets.mapIndexed { index, bucketDate ->
            val totalPatrimonyForPeriod = patrimonyAssetBreakdown.values.sumOf { points -> points[index].networth } - patrimonyLiabilityBreakdown.values.sumOf { points -> abs(points[points.lastIndex].networth)  }
            val historicalSavings = savingGoalBalancesPerPeriod[bucketDate] ?: 0.0

            NetWorthPeriodOutput(
                date = bucketDate,
                networth = totalPatrimonyForPeriod + historicalSavings
            )
        }

        return GetPatrimonyEvolutionOutput(
            networthByPeriod = aggregatedTotals,
            breakdown = patrimonyAssetBreakdown + patrimonyLiabilityBreakdown
        )
    }

    /**
     * Generates dates corresponding to period intervals (e.g. 1st of every month for N intervals)
     */
    private fun generatePeriodBuckets(startDate: LocalDate, periodType: PeriodType, count: Int): List<LocalDate> {
        return (0 until count).map { step ->
            when (periodType) {
                PeriodType.MONTH -> startDate.plusMonths(step.toLong()).with(TemporalAdjusters.firstDayOfMonth())
                PeriodType.WEEK -> startDate.plusWeeks(step.toLong())
                PeriodType.YEAR -> startDate.plusYears(step.toLong()).with(TemporalAdjusters.firstDayOfYear())
                else -> startDate.plusDays(step.toLong())
            }
        }
    }
}