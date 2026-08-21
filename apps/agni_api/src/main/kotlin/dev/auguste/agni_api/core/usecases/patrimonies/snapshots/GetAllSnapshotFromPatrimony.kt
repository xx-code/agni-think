package dev.auguste.agni_api.core.usecases.patrimonies.snapshots

import dev.auguste.agni_api.core.SAVING_CATEGORY_ID
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryPatrimonySnapshotExtend
import dev.auguste.agni_api.core.entities.PatrimonySnapshot
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.entities.enums.PatrimonySnapshotStatusType
import dev.auguste.agni_api.core.entities.enums.PeriodType
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceByPeriodOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalancesByPeriodInput
import dev.auguste.agni_api.core.usecases.patrimonies.snapshots.dto.GetAllSnapshotPatrimonyInput
import dev.auguste.agni_api.core.usecases.patrimonies.snapshots.dto.GetSnapshotPatrimonyOutput
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import java.util.UUID

class GetAllSnapshotFromPatrimony(
    private val snapshotPatrimonyRepo: IRepository<PatrimonySnapshot>,
    private val savingGoalRepo: IRepository<SavingGoal>,
    private val getBalanceByPeriod: IUseCase<GetBalancesByPeriodInput, List<GetBalanceByPeriodOutput>>
): IUseCase<GetAllSnapshotPatrimonyInput, ListOutput<GetSnapshotPatrimonyOutput>> {

    override fun execAsync(input: GetAllSnapshotPatrimonyInput): ListOutput<GetSnapshotPatrimonyOutput> {
        if (input.isFund) {
            val numMonth: Long = 6
            val currentSavingGoalBalance = savingGoalRepo.getAll(QueryFilter.queryAll()).items.sumOf { it.balance }
            val date = LocalDateTime.now().minusMonths(numMonth).with(TemporalAdjusters.firstDayOfMonth())
            val periodBuckets = (0 until numMonth).map { step ->
                date.plusMonths(step).with(TemporalAdjusters.firstDayOfMonth())
            }

            val savingBalancesByDate = getBalanceByPeriod.execAsync(
                GetBalancesByPeriodInput(
                    period = PeriodType.MONTH,
                    interval = numMonth.toInt(),
                    dateFrom = date,
                    categoryIds = setOf(SAVING_CATEGORY_ID),
                    status = InvoiceStatusType.COMPLETED
                )
            ).associateBy { it.date }

            // Compute total change from start date to current date
            var cumulativeBalance = currentSavingGoalBalance

            // Build period-by-period accumulated balance list
            val savingGoalBalancesPerPeriod = periodBuckets.sortedByDescending { it }.map { bucketDate ->
                val savingIn = savingBalancesByDate[bucketDate.toLocalDate()]?.spend ?: 0.0
                val savingOut = savingBalancesByDate[bucketDate.toLocalDate()]?.income ?: 0.0

                val delta = savingIn - savingOut

                cumulativeBalance -= delta
                bucketDate to cumulativeBalance
            }.toMap()

            return ListOutput(
                items = savingGoalBalancesPerPeriod.map { GetSnapshotPatrimonyOutput(
                    id = UUID.randomUUID(),
                    patrimonyId = UUID.randomUUID(),
                    balance = it.value,
                    date = it.key.toLocalDate(),
                    status = PatrimonySnapshotStatusType.COMPLETED.value
                ) },
                total = savingGoalBalancesPerPeriod.size.toLong()
            )
        }

        input.query.sortBy.by = "date"
        val snapshots = snapshotPatrimonyRepo.getAll(input.query, QueryPatrimonySnapshotExtend(setOf(input.patrimonyId)))

        return ListOutput(
            items = snapshots.items.map {
                GetSnapshotPatrimonyOutput(
                    id = it.id,
                    patrimonyId = it.patrimonyId,
                    date = it.date,
                    status = it.status.value,
                    balance = it.currentBalanceObserved
                )
            },
            total = snapshots.total
        )
    }
}