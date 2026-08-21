package dev.auguste.agni_api.core.usecases.patrimonies

import dev.auguste.agni_api.core.SAVING_CATEGORY_ID
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.dto.QuerySortBy
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryPatrimonySnapshotExtend
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.Patrimony
import dev.auguste.agni_api.core.entities.PatrimonySnapshot
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.entities.enums.PeriodType
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.entities.enums.PatrimonyType
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceByPeriodOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalancesByPeriodInput
import dev.auguste.agni_api.core.usecases.patrimonies.dto.GetPatrimonyInput
import dev.auguste.agni_api.core.usecases.patrimonies.dto.GetPatrimonyOutput
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import java.util.UUID

class GetPatrimony(
    private val patrimonyRepo: IRepository<Patrimony>,
    private val accountRepo: IRepository<Account>,
    private val savingGoalRepo: IRepository<SavingGoal>,
    private val patrimonySnapshotRepo: IRepository<PatrimonySnapshot>,
    private val getBalancesByPeriod: IUseCase<GetBalancesByPeriodInput, List<GetBalanceByPeriodOutput>>
) : IUseCase<GetPatrimonyInput, GetPatrimonyOutput> {

    override fun execAsync(input: GetPatrimonyInput): GetPatrimonyOutput {
        if (input.isTotalFund) {
            val savingGoals = savingGoalRepo.getAll(QueryFilter(0, 0, true))
            val savingGoalAmount = savingGoals.items.sumOf { it.balance }
            val balancesByPeriodSavingGoal = getBalancesByPeriod.execAsync(
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
                isTotalFund = true
            )
        }

        val patrimony = patrimonyRepo.get(input.id) ?: throw DomainException.NotFound.Patrimony(input.id)

        val snapshots = patrimonySnapshotRepo.getAll(
            QueryFilter(0, 0, true, QuerySortBy("date")),
            QueryPatrimonySnapshotExtend(setOf(input.id))
        )

        val accounts = accountRepo.getManyByIds(patrimony.accountIds)
        val balancesByPeriod = getBalancesByPeriod.execAsync(
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
            type = patrimony.type.value
        )
    }
}