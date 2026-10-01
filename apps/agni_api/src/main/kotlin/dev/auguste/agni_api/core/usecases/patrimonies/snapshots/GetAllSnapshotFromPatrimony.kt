package dev.auguste.agni_api.core.usecases.patrimonies.snapshots

import dev.auguste.agni_api.core.SAVING_CATEGORY_ID
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.QueryExtendBuilder
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryComparator
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryPatrimonySnapshotExtend
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.entities.PatrimonySnapshot
import dev.auguste.agni_api.core.entities.Provision
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.entities.enums.InvoiceModuleLinkerType
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.entities.enums.PatrimonySnapshotStatusType
import dev.auguste.agni_api.core.entities.enums.PeriodType
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceByPeriodOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalancesByPeriodInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetInvoiceOutput
import dev.auguste.agni_api.core.usecases.patrimonies.dto.SourcePatrimonyType
import dev.auguste.agni_api.core.usecases.patrimonies.snapshots.dto.GetAllSnapshotPatrimonyInput
import dev.auguste.agni_api.core.usecases.patrimonies.snapshots.dto.GetSnapshotPatrimonyOutput
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.UUID

class GetAllSnapshotFromPatrimony(
    private val snapshotPatrimonyRepo: IRepository<PatrimonySnapshot>,
    private val savingGoalRepo: IRepository<SavingGoal>,
    private val getBalanceByPeriod: IUseCase<GetBalancesByPeriodInput, List<GetBalanceByPeriodOutput>>,
    private val provisionRepo: IRepository<Provision>,
    private val invoiceRepo: IRepository<Invoice>,
    private val getManyInvoices: IUseCase<Set<UUID>, List<GetInvoiceOutput>>
): IUseCase<GetAllSnapshotPatrimonyInput, ListOutput<GetSnapshotPatrimonyOutput>> {

    override fun execAsync(input: GetAllSnapshotPatrimonyInput): ListOutput<GetSnapshotPatrimonyOutput> {
        when (input.sourcePatrimonyType) {
            SourcePatrimonyType.PATRIMONY -> {
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
            SourcePatrimonyType.FUND -> {
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
            SourcePatrimonyType.PROVISION -> {
                val provision = provisionRepo.get(input.patrimonyId) ?: throw DomainException.NotFound.Provisionable(input.patrimonyId)

                val snapshots = mutableListOf<GetSnapshotPatrimonyOutput>()
                if (!input.isAsset) {
                    val conditionInvoice = QueryExtendBuilder<Invoice>()
                        .addCondition(
                            "moduleLinkers.module",
                            QueryComparator.Equal,
                            InvoiceModuleLinkerType.PROVISION.value
                        )
                        .addCondition(
                            "moduleLinkers.sourceId",
                            QueryComparator.Equal,
                            provision.id
                        )


                    val invoices = invoiceRepo.getAll(
                        input.query,
                        conditionInvoice
                    )

                    val firstInvoiceMonthStart = invoices.items.minOf{ it.date }.with(TemporalAdjusters.firstDayOfMonth())
                    val notRegisterAmount = provision.paymentInfo?.let { paymentInfo ->
                        val occurrencePayment = paymentInfo.scheduler.repeater?.computeOccurrences(provision.acquisitionDate, firstInvoiceMonthStart.toLocalDate()) ?: 0
                        return@let occurrencePayment * paymentInfo.paymentAmount
                    } ?: 0.0

                    val totalCost = provision.calculateTotalCost()

                    snapshots.add(GetSnapshotPatrimonyOutput(
                        id = UUID.randomUUID(),
                        patrimonyId = provision.id,
                        balance = totalCost,
                        date = provision.acquisitionDate,
                        status = PatrimonySnapshotStatusType.COMPLETED.value
                    ))

                    if (notRegisterAmount > 0)
                        snapshots.add(GetSnapshotPatrimonyOutput(
                            id = UUID.randomUUID(),
                            patrimonyId = provision.id,
                            balance = notRegisterAmount,
                            date = firstInvoiceMonthStart.toLocalDate(),
                            status = PatrimonySnapshotStatusType.COMPLETED.value
                        ))

                    if (invoices.items.isNotEmpty()) {
                        val invoices = getManyInvoices.execAsync(
                            invoices.items.map { it.id }.toSet(),
                        ).toMutableList()

                        invoices.forEach {
                            snapshots.add(
                                GetSnapshotPatrimonyOutput(
                                    id = UUID.randomUUID(),
                                    patrimonyId = provision.id,
                                    balance = totalCost - it.total + notRegisterAmount,
                                    date = it.date.toLocalDate(),
                                    status = PatrimonySnapshotStatusType.COMPLETED.value
                                )
                            )
                        }
                    }
                } else {
                    val monthsBetween = ChronoUnit.MONTHS.between(provision.acquisitionDate, LocalDate.now())
                    for (i in 0..monthsBetween) {
                        val date = provision.acquisitionDate.plusMonths(i)
                        snapshots.add(GetSnapshotPatrimonyOutput(
                            id = UUID.randomUUID(),
                            patrimonyId = provision.id,
                            balance = provision.calculateResidualValue(date),
                            date = date,
                            status = PatrimonySnapshotStatusType.COMPLETED.value
                        ))
                    }
                }

                return ListOutput(
                    items = snapshots.sortedByDescending { it.date },
                    total = snapshots.size.toLong()
                )
            }
        }
    }
}