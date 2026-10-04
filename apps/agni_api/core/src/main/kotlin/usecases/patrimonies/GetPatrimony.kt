package usecases.patrimonies

import usecases.UseCase
import domain.SAVING_CATEGORY_ID
import adapters.dto.QueryFilter
import adapters.dto.QuerySortBy
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Account
import domain.entities.Patrimony
import domain.entities.PatrimonySnapshot
import usecases.interfaces.IUseCase
import domain.exceptions.NotFoundException
import domain.entities.Invoice
import domain.entities.Provision
import domain.entities.Fund
import usecases.invoices.dto.GetBalanceByPeriodOutput
import usecases.invoices.dto.GetBalancesByPeriodInput
import usecases.invoices.dto.GetInvoiceOutput
import usecases.patrimonies.dto.GetPatrimonyInput
import usecases.patrimonies.dto.GetPatrimonyOutput
import usecases.patrimonies.dto.SourcePatrimonyType
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import java.util.UUID
import domain.enums.InvoiceModuleLinkerType
import domain.enums.InvoiceStatusType
import domain.enums.PatrimonyType
import domain.enums.PeriodType

class GetPatrimony(
    private val patrimonyRepo: IRepository<Patrimony>,
    private val accountRepo: IRepository<Account>,
    private val fundRepo: IRepository<Fund>,
    private val patrimonySnapshotRepo: IRepository<PatrimonySnapshot>,
    private val provisionRepo: IRepository<Provision>,
    private val invoiceRepo: IRepository<Invoice>,
    private val getBalancesByPeriod: IUseCase<GetBalancesByPeriodInput, List<GetBalanceByPeriodOutput>>,
    private val getManyInvoices: IUseCase<Set<UUID>, List<GetInvoiceOutput>>
): UseCase<GetPatrimonyInput, GetPatrimonyOutput>() {

    override suspend fun process(input: GetPatrimonyInput): GetPatrimonyOutput {
        when(input.sourceType) {
            SourcePatrimonyType.PROVISION -> {
                val provision = provisionRepo.get(input.id) ?: throw NotFoundException.SingleEntity(input.id, "provisionable")
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
                    QueryFilter.queryAll(),
                    conditionInvoice
                )

                val now = LocalDateTime.now()
                val currentMonthStart = now.with(TemporalAdjusters.firstDayOfMonth())
                val previousMonthStart = currentMonthStart.minusMonths(1)
                val firstInvoiceMonthStart = invoices.items.minOf{ it.date }.with(TemporalAdjusters.firstDayOfMonth())
                val notRegisterAmount = provision.paymentInfo?.let { paymentInfo ->
                    val occurrencePayment = paymentInfo.scheduler.repeater?.computeOccurrences(provision.acquisitionDate, firstInvoiceMonthStart.toLocalDate()) ?: 0
                    return@let occurrencePayment * paymentInfo.paymentAmount
                } ?: 0.0
                val lastMonthInvoices = invoices.items
                var detailInvoices = mutableListOf<GetInvoiceOutput>()
                if (lastMonthInvoices.isNotEmpty()) {
                    detailInvoices = getManyInvoices.processDirect(
                    lastMonthInvoices.map { it.id }.toSet(),
                    ).toMutableList()
                }

                val totalCost = provision.calculateTotalCost()

                val passInvoicePayment = detailInvoices.sumOf { it.total } + notRegisterAmount
                val lastMonthInvoiceAmount = detailInvoices.filter{ it.date <= previousMonthStart }.sumOf { it.total } + notRegisterAmount
                val pastBalance = if (input.isAsset) provision.calculateResidualValue(previousMonthStart.toLocalDate()) else totalCost - lastMonthInvoiceAmount
                val currentBalance = if (input.isAsset) provision.calculateResidualValue() else totalCost - passInvoicePayment

                return GetPatrimonyOutput(
                    id = provision.id,
                    title = provision.title,
                    accountIds = provision.paymentInfo
                        ?.let { listOf(it.accountId) }
                        ?: listOf(),
                    amount = if (input.isAsset) 0.0 else provision.calculateTotalCost(),
                    currentBalance = currentBalance,
                    pastBalance = pastBalance,
                    type = PatrimonyType.LIABILITY.value,
                    sourceType = SourcePatrimonyType.PROVISION.value
                )
            }
            SourcePatrimonyType.FUND -> {
                val savingGoals = fundRepo.getAll(QueryFilter(0, 0, true))
                val savingGoalAmount = savingGoals.items.sumOf { it.balance }
                val balancesByPeriodSavingGoal = getBalancesByPeriod.processDirect(
                    GetBalancesByPeriodInput(
                        period = PeriodType.MONTH,
                        interval = 1,
                        dateFrom = LocalDateTime.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()),
                        categoryIds = setOf(SAVING_CATEGORY_ID),
                        status = InvoiceStatusType.COMPLETED
                    )
                )

                val passSavingGoalTransactionBalance = if (balancesByPeriodSavingGoal.size > 1) {
                    balancesByPeriodSavingGoal.first().spend - balancesByPeriodSavingGoal.first().income
                } else 0.0

                val passSavingGoalBalance = savingGoalAmount - passSavingGoalTransactionBalance

                return GetPatrimonyOutput(
                    id = UUID.randomUUID(),
                    title = "Fond d'épargne",
                    amount = savingGoalAmount,
                    currentBalance = savingGoalAmount,
                    pastBalance = if (passSavingGoalBalance > 0) passSavingGoalBalance else 0.0,
                    type = PatrimonyType.ASSET.value,
                    accountIds = listOf(),
                    sourceType = SourcePatrimonyType.FUND.value
                )
            }
            SourcePatrimonyType.PATRIMONY -> {
                val patrimony = patrimonyRepo.get(input.id) ?: throw NotFoundException.SingleEntity(input.id, "patrimony")

                val conditionSnapShot = QueryExtendBuilder<PatrimonySnapshot>()
                    .addCondition("patrimonyId", QueryComparator.Equal, input.id)
                val snapshots = patrimonySnapshotRepo.getAll(
                    QueryFilter(0, 0, true, QuerySortBy("date")),
                    conditionSnapShot
                )

                val accounts = accountRepo.getManyByIds(patrimony.accountIds)
                val balancesByPeriod = getBalancesByPeriod.processDirect(
                    GetBalancesByPeriodInput(
                        period = PeriodType.MONTH,
                        interval = 1,
                        dateFrom = LocalDateTime.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()),
                        accountIds = patrimony.accountIds.toSet(),
                        status = InvoiceStatusType.COMPLETED
                    )
                )

                val accountBalance = accounts.sumOf { it.balance }
                val accountPastBalance = if (balancesByPeriod.isNotEmpty())
                    balancesByPeriod.first().balance else 0.0

                val patrimonySnapshots = snapshots.items.filter { it.patrimonyId == patrimony.id }

                val currentSnapshot = if (patrimonySnapshots.isNotEmpty())
                    patrimonySnapshots.first().currentBalanceObserved else accountBalance

                val pastSnapshot = if (patrimonySnapshots.size > 1)
                    patrimonySnapshots[1].currentBalanceObserved else accountPastBalance

                val amount = patrimony.amount + accountBalance

                return GetPatrimonyOutput(
                    id = patrimony.id,
                    title = patrimony.title,
                    amount = amount,
                    accountIds = patrimony.accountIds.toList(),
                    currentBalance = currentSnapshot,
                    pastBalance = pastSnapshot,
                    type = patrimony.type.value,
                    sourceType = SourcePatrimonyType.PATRIMONY.value
                )
            }
        }
    }
}