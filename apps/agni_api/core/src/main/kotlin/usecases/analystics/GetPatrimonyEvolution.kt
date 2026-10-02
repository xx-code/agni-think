package usecases.analystics

import domain.SAVING_CATEGORY_ID
import adapters.dto.QueryFilter
import adapters.dto.QuerySortBy
import adapters.repositories.IRepository
import adapters.repositories.query_extend.QueryPatrimonySnapshotExtend
import domain.entities.Patrimony
import domain.entities.PatrimonySnapshot
import domain.entities.Fund
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


class GetPatrimonyEvolution(
    private val patrimonyRepo: IRepository<Patrimony>,
    private val patrimonySnapshotRepo: IRepository<PatrimonySnapshot>,
    private val fundRepo: IRepository<Fund>,
    private val getBalanceByPeriod: IUseCase<GetBalancesByPeriodInput, List<GetBalanceByPeriodOutput>>
) : IUseCase<GetPatrimonyEvolutionInput, GetPatrimonyEvolutionOutput> {

    override fun execAsync(input: GetPatrimonyEvolutionInput): GetPatrimonyEvolutionOutput {
        val patrimonies = patrimonyRepo.getAll(QueryFilter.queryAll())
        val patrimonyIds = patrimonies.items.map { it.id }.toSet()

        // 1. Fetch all relevant snapshots sorted chronologically
        val snapshots = patrimonySnapshotRepo.getAll(
            QueryFilter(0, 0, true, QuerySortBy("date")),
            QueryPatrimonySnapshotExtend(patrimonyIds)
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
            val balancesByPeriod = getBalanceByPeriod.execAsync(
                GetBalancesByPeriodInput(
                    period = input.periodType,
                    interval = input.interval,
                    dateFrom = date,
                    accountIds = patrimony.accountIds.toSet(),
                    status = _root_ide_package_.domain.enums.InvoiceStatusType.COMPLETED
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
            if (patrimony.type == _root_ide_package_.domain.enums.PatrimonyType.LIABILITY)
                patrimonyLiabilityBreakdown[patrimony.id] = evolutionPoints
            else
                patrimonyAssetBreakdown[patrimony.id] = evolutionPoints
        }

        // 4. Get actual total balance today
        val currentSavingGoalBalance = fundRepo.getAll(QueryFilter.queryAll()).items.sumOf { it.balance }

        // Fetch periodic savings activity
        val savingBalancesByDate = getBalanceByPeriod.execAsync(
            GetBalancesByPeriodInput(
                period = input.periodType,
                interval = input.interval,
                dateFrom = date,
                categoryIds = setOf(SAVING_CATEGORY_ID),
                status = _root_ide_package_.domain.enums.InvoiceStatusType.COMPLETED
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
    private fun generatePeriodBuckets(startDate: LocalDate, periodType: domain.enums.PeriodType, count: Int): List<LocalDate> {
        return (0 until count).map { step ->
            when (periodType) {
                _root_ide_package_.domain.enums.PeriodType.MONTH -> startDate.plusMonths(step.toLong()).with(TemporalAdjusters.firstDayOfMonth())
                _root_ide_package_.domain.enums.PeriodType.WEEK -> startDate.plusWeeks(step.toLong())
                _root_ide_package_.domain.enums.PeriodType.YEAR -> startDate.plusYears(step.toLong()).with(TemporalAdjusters.firstDayOfYear())
                else -> startDate.plusDays(step.toLong())
            }
        }
    }
}