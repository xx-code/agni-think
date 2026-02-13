package dev.auguste.agni_api.core.usecases.patrimonies

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryPatrimonySnapshotExtend
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.Patrimony
import dev.auguste.agni_api.core.entities.PatrimonySnapshot
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.entities.enums.PatrimonyType
import dev.auguste.agni_api.core.entities.enums.PeriodType
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalancesByPeriodInput
import dev.auguste.agni_api.core.usecases.patrimonies.dto.GetPatrimonyOutput
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import java.util.UUID

class GetAllPatrimonies(
    private val patrimonyRepo: IRepository<Patrimony>,
    private val accountRepo: IRepository<Account>,
    private val patrimonySnapshotRepo: IRepository<PatrimonySnapshot>,
    private val savingGoalRepo: IRepository<SavingGoal>,
    private val getBalanceByPeriod: IUseCase<GetBalancesByPeriodInput, List<GetBalanceOutput>>): IUseCase<QueryFilter, ListOutput<GetPatrimonyOutput>> {

    override fun execAsync(input: QueryFilter): ListOutput<GetPatrimonyOutput> {
        val patrimonies = patrimonyRepo.getAll(input)

        val snapshots = patrimonySnapshotRepo.getAll(
            QueryFilter(0,0,true),
            QueryPatrimonySnapshotExtend(patrimonies.items.map { it.id }.toSet()))

        val results = mutableListOf<GetPatrimonyOutput>()

        for (patrimony in patrimonies.items) {
            val accounts = accountRepo.getManyByIds(patrimony.accountIds)
            val balancesByPeriod = getBalanceByPeriod.execAsync(GetBalancesByPeriodInput(
                period = PeriodType.MONTH,
                interval = 1,
                dateFrom = LocalDateTime.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()),
                accountIds = patrimony.accountIds.toSet(),
                status = InvoiceStatusType.COMPLETED
            ))

            val accountBalance = accounts.sumOf { it.balance }
            val accountPastBalance = if (balancesByPeriod.isNotEmpty())
                balancesByPeriod.first().balance else 0.0

            val patrimonySnapshots = snapshots.items.filter { it.patrimonyId == patrimony.id }

            val currentSnapshot = if (patrimonySnapshots.isNotEmpty())
                patrimonySnapshots.first().currentBalanceObserved else accountBalance

            val pastSnapshot = if (patrimonySnapshots.size > 1)
                patrimonySnapshots.last().currentBalanceObserved else accountPastBalance

            val amount = patrimony.amount + accountBalance

            results.add(GetPatrimonyOutput(
                id = patrimony.id,
                title = patrimony.title,
                amount = amount,
                accountIds = patrimony.accountIds.toList(),
                currentBalance = currentSnapshot,
                pastBalance = pastSnapshot,
                type = patrimony.type.value
            ))
        }

        val savingGoals = savingGoalRepo.getAll(QueryFilter(0, 0, true))
        val savingGoalAmount = savingGoals.items.sumOf { it.balance }

        results.add(GetPatrimonyOutput(
            id = UUID.randomUUID(),
            title = "Epargne Saving Goal",
            amount = savingGoalAmount,
            currentBalance = savingGoalAmount,
            pastBalance = savingGoalAmount,
            type = PatrimonyType.ASSET.value,
            accountIds = listOf()
        ))

        return ListOutput(
            items = results,
            total = patrimonies.total
        )
    }
}