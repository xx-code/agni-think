package usecases.patrimonies.snapshots

import domain.SAVING_CATEGORY_ID
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.query_extend.QueryComparator
import adapters.repositories.query_extend.QueryPatrimonySnapshotExtend
import domain.exceptions.NotFoundException
import domain.entities.Invoice
import domain.entities.PatrimonySnapshot
import domain.entities.Provision
import domain.entities.Fund
import usecases.ListOutput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetBalanceByPeriodOutput
import usecases.invoices.dto.GetBalancesByPeriodInput
import usecases.invoices.dto.GetInvoiceOutput
import usecases.patrimonies.dto.SourcePatrimonyType
import usecases.patrimonies.snapshots.dto.GetAllSnapshotPatrimonyInput
import usecases.patrimonies.snapshots.dto.GetSnapshotPatrimonyOutput
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.UUID

class GetAllSnapshotFromPatrimony(
    private val snapshotPatrimonyRepo: IRepository<PatrimonySnapshot>,
    private val fundRepo: IRepository<Fund>,
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
                val currentSavingGoalBalance = fundRepo.getAll(QueryFilter.queryAll()).items.sumOf { it.balance }
                val date = LocalDateTime.now().minusMonths(numMonth).with(TemporalAdjusters.firstDayOfMonth())
                val periodBuckets = (0 until numMonth).map { step ->
                    date.plusMonths(step).with(TemporalAdjusters.firstDayOfMonth())
                }

                val savingBalancesByDate = getBalanceByPeriod.execAsync(
                    GetBalancesByPeriodInput(
                        period = _root_ide_package_.domain.enums.PeriodType.MONTH,
                        interval = numMonth.toInt(),
                        dateFrom = date,
                        categoryIds = setOf(SAVING_CATEGORY_ID),
                        status = _root_ide_package_.domain.enums.InvoiceStatusType.COMPLETED
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
                        status = _root_ide_package_.domain.enums.PatrimonySnapshotStatusType.COMPLETED.value
                    ) },
                    total = savingGoalBalancesPerPeriod.size.toLong()
                )
            }
            SourcePatrimonyType.PROVISION -> {
                val provision = provisionRepo.get(input.patrimonyId) ?: throw NotFoundException.SingleEntity(input.patrimonyId, "provisionable")

                val snapshots = mutableListOf<GetSnapshotPatrimonyOutput>()
                if (!input.isAsset) {
                    val conditionInvoice = QueryExtendBuilder<Invoice>()
                        .addCondition(
                            "moduleLinkers.module",
                            QueryComparator.Equal,
                            _root_ide_package_.domain.enums.InvoiceModuleLinkerType.PROVISION.value
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
                        status = _root_ide_package_.domain.enums.PatrimonySnapshotStatusType.COMPLETED.value
                    ))

                    if (notRegisterAmount > 0)
                        snapshots.add(GetSnapshotPatrimonyOutput(
                            id = UUID.randomUUID(),
                            patrimonyId = provision.id,
                            balance = notRegisterAmount,
                            date = firstInvoiceMonthStart.toLocalDate(),
                            status = _root_ide_package_.domain.enums.PatrimonySnapshotStatusType.COMPLETED.value
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
                                    status = _root_ide_package_.domain.enums.PatrimonySnapshotStatusType.COMPLETED.value
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
                            status = _root_ide_package_.domain.enums.PatrimonySnapshotStatusType.COMPLETED.value
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