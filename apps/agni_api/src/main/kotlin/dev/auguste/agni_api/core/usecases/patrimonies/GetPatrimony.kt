package dev.auguste.agni_api.core.usecases.patrimonies

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QuerySnapshotPatrimonyExtend
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.Patrimony
import dev.auguste.agni_api.core.entities.PatrimonySnapshot
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.entities.enums.PeriodType
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalancesByPeriodInput
import dev.auguste.agni_api.core.usecases.patrimonies.dto.GetPatrimonyOutput
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import java.util.UUID

class GetPatrimony(
    val patrimonyRepo: IRepository<Patrimony>,
    val accountRepo: IRepository<Account>,
    val patrimonySnapshotRepo: IRepository<PatrimonySnapshot>,
    val getBalancesByPeriod: IUseCase<GetBalancesByPeriodInput, List<GetBalanceOutput>>
) : IUseCase<UUID, GetPatrimonyOutput> {

    override fun execAsync(input: UUID): GetPatrimonyOutput {
        val patrimony = patrimonyRepo.get(input) ?: throw Error("Patrimony not found")

        val snapshots = patrimonySnapshotRepo.getAll(
            QueryFilter(0, 0, true),
            QuerySnapshotPatrimonyExtend(setOf(input))
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
            patrimonySnapshots.last().currentBalanceObserved else accountPastBalance

        val amount = patrimony.amount + accountBalance

        return GetPatrimonyOutput(
            id = patrimony.id,
            title = patrimony.title,
            amount = amount,
            accountIds = patrimony.accountIds.toList(),
            currentBalance = currentSnapshot,
            pastBalance = pastSnapshot,
            type = patrimony.type
        )
    }
}