package dev.auguste.agni_api.core.usecases.patrimonies

import dev.auguste.agni_api.core.SAVING_CATEGORY_ID
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.dto.QuerySortBy
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.QueryExtendBuilder
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryComparator
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryPatrimonySnapshotExtend
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.Invoice
import dev.auguste.agni_api.core.entities.Patrimony
import dev.auguste.agni_api.core.entities.PatrimonySnapshot
import dev.auguste.agni_api.core.entities.Provision
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.entities.enums.InvoiceModuleLinkerType
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.entities.enums.PatrimonyType
import dev.auguste.agni_api.core.entities.enums.PeriodType
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceByPeriodOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalancesByPeriodInput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetInvoiceOutput
import dev.auguste.agni_api.core.usecases.invoices.transactions.dto.GetInvoiceTransactionsOutput
import dev.auguste.agni_api.core.usecases.patrimonies.dto.GetPatrimonyOutput
import dev.auguste.agni_api.core.usecases.patrimonies.dto.SourcePatrimonyType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.UUID

class GetAllPatrimonies(
    private val patrimonyRepo: IRepository<Patrimony>,
    private val accountRepo: IRepository<Account>,
    private val patrimonySnapshotRepo: IRepository<PatrimonySnapshot>,
    private val savingGoalRepo: IRepository<SavingGoal>,
    private val provisionRepo: IRepository<Provision>,
    private val invoiceRepo: IRepository<Invoice>,
    private val getBalanceByPeriod: IUseCase<GetBalancesByPeriodInput, List<GetBalanceByPeriodOutput>>,
    private val getManyInvoices: IUseCase<Set<UUID>, List<GetInvoiceOutput>>): IUseCase<QueryFilter, ListOutput<GetPatrimonyOutput>> {

    override fun execAsync(input: QueryFilter): ListOutput<GetPatrimonyOutput> {
        val patrimonies = patrimonyRepo.getAll(input)

        val conditionProvision = QueryExtendBuilder<Provision>()
        conditionProvision.addCondition("isPatrimony", QueryComparator.Equal, true)
        val provisions = provisionRepo.getAll(QueryFilter.queryAll(), conditionProvision)

        val snapshots = patrimonySnapshotRepo.getAll(
            QueryFilter(0,0,true, QuerySortBy("date")),
            QueryPatrimonySnapshotExtend(patrimonies.items.map { it.id }.toSet())
        )

        val results = mutableListOf<GetPatrimonyOutput>()
        val accounts = accountRepo.getManyByIds(patrimonies.items.flatMap { it.accountIds }.toSet())
        val startDate = LocalDateTime.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth())

        for (patrimony in patrimonies.items) {
            val patrimonyAccounts = accounts.filter { patrimony.accountIds.contains(it.id) }
            val balancesByPeriod = getBalanceByPeriod.execAsync(GetBalancesByPeriodInput(
                period = PeriodType.MONTH,
                interval = 1,
                dateFrom = startDate,
                accountIds = patrimony.accountIds.toSet(),
                status = InvoiceStatusType.COMPLETED
            ))

            val accountBalance = patrimonyAccounts.sumOf { it.balance }
            val accountPastBalance = if (balancesByPeriod.isNotEmpty())
                balancesByPeriod.first().balance else 0.0

            val patrimonySnapshots = snapshots.items.filter { it.patrimonyId == patrimony.id }

            val currentSnapshot = if (patrimonySnapshots.isNotEmpty())
                patrimonySnapshots.first().currentBalanceObserved else accountBalance

            val pastSnapshot = if (patrimonySnapshots.size > 1)
                patrimonySnapshots[1].currentBalanceObserved else accountPastBalance

            val amount = patrimony.amount + accountBalance

            results.add(GetPatrimonyOutput(
                id = patrimony.id,
                title = patrimony.title,
                amount = amount,
                accountIds = patrimony.accountIds.toList(),
                currentBalance = currentSnapshot,
                pastBalance = pastSnapshot,
                type = patrimony.type.value,
                sourceType = SourcePatrimonyType.PATRIMONY.value,
            ))
        }

        val savingGoals = savingGoalRepo.getAll(QueryFilter(0, 0, true))
        val savingGoalAmount = savingGoals.items.sumOf { it.balance }
        val balancesByPeriodSavingGoal = getBalanceByPeriod.execAsync(
            GetBalancesByPeriodInput(
                period = PeriodType.MONTH,
                interval = 1,
                dateFrom = startDate,
                categoryIds = setOf(SAVING_CATEGORY_ID),
                status = InvoiceStatusType.COMPLETED
            )
        )

        val passSavingGoalTransactionBalance = if (balancesByPeriodSavingGoal.size > 1) {
            balancesByPeriodSavingGoal.first().spend - balancesByPeriodSavingGoal.first().income
        } else 0.0

        val passSavingGoalBalance = savingGoalAmount - passSavingGoalTransactionBalance

        results.add(GetPatrimonyOutput(
            id = UUID.randomUUID(),
            title = "Fond d'épargne",
            amount = savingGoalAmount,
            currentBalance = savingGoalAmount,
            pastBalance = if (passSavingGoalBalance > 0) passSavingGoalBalance else 0.0,
            type = PatrimonyType.ASSET.value,
            accountIds = listOf(),
            sourceType = SourcePatrimonyType.FUND.value
        ))

        // Provision compute
        val now = LocalDateTime.now()
        val currentMonthStart = now.with(TemporalAdjusters.firstDayOfMonth())
        val previousMonthStart = currentMonthStart.minusMonths(1)

        val provisionIds = provisions.items.map { it.id }.toSet()

        val conditionInvoice = QueryExtendBuilder<Invoice>()
            .addCondition(
                "moduleLinkers.module",
                QueryComparator.Equal,
                InvoiceModuleLinkerType.PROVISION.value
            )
            .addCondition(
                "moduleLinkers.sourceId",
                QueryComparator.In,
                provisionIds
            )

        val invoices = invoiceRepo.getAll(
            QueryFilter.queryAll(),
            conditionInvoice
        )

        // Last invoice of each provision from the previous month
        val lastMonthInvoices = invoices.items
            .groupBy { invoice ->
                invoice.moduleLinkers
                    ?.firstOrNull { it.module == InvoiceModuleLinkerType.PROVISION }
                    ?.sourceId
            }
            .mapNotNull { (provisionId, invoices) ->
                provisionId?.let { id ->
                    id to invoices
                }
            }
            .toMap()

        var detailInvoiceTransactions = mutableListOf<GetInvoiceOutput>()
        if (lastMonthInvoices.isNotEmpty()) {
            detailInvoiceTransactions = getManyInvoices.execAsync(
            lastMonthInvoices.values
                .flatMap { it.map { inv -> inv.id } }
                .toSet()
            ).toMutableList()
        }

        for (provision in provisions.items) {
            val currentResidual = provision.calculateResidualValue()

            val notRegisterAmount = provision.paymentInfo?.let { paymentInfo ->
                val occurrencePayment = paymentInfo.scheduler.repeater?.computeOccurrences(provision.acquisitionDate, previousMonthStart.toLocalDate()) ?: 0
                return@let occurrencePayment * paymentInfo.paymentAmount
            } ?: 0.0

            val detailInvoices = detailInvoiceTransactions.filter { lastMonthInvoices[provision.id]?.map { inv -> inv.id }?.contains(it.id) ?: false }
            val passInvoicePayment = detailInvoices.sumOf { it.total } + notRegisterAmount
            val lastMonthInvoiceAmount = detailInvoices.filter { it.date <= previousMonthStart } .sumOf { it.total }

            val totalCost = provision.calculateTotalCost()

            val passAsset = provision.calculateResidualValue(previousMonthStart.toLocalDate())
            val passLiability = totalCost - (lastMonthInvoiceAmount + notRegisterAmount)

            results.add(
                GetPatrimonyOutput(
                    id = provision.id,
                    title = provision.title,
                    accountIds = provision.paymentInfo
                        ?.let { listOf(it.accountId) }
                        ?: listOf(),
                    amount = 0.0,
                    currentBalance = currentResidual,
                    pastBalance = passAsset,
                    type = PatrimonyType.ASSET.value,
                    sourceType = SourcePatrimonyType.PROVISION.value
                )
            )

            results.add(
                GetPatrimonyOutput(
                    id = provision.id,
                    title = provision.title,
                    accountIds = provision.paymentInfo
                        ?.let { listOf(it.accountId) }
                        ?: listOf(),
                    amount = totalCost,
                    currentBalance = totalCost - passInvoicePayment,
                    pastBalance = passLiability,
                    type = PatrimonyType.LIABILITY.value,
                    sourceType = SourcePatrimonyType.PROVISION.value
                )
            )
        }

        return ListOutput(
            items = results,
            total = patrimonies.total
        )
    }
}