package usecases.accounts

import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.QueryComparator
import adapters.repositories.QueryExtendBuilder
import domain.entities.Account
import domain.entities.Fund
import domain.entities.Profile
import domain.entities.ScheduleInvoice
import domain.enums.AccountType
import domain.enums.InvoiceType
import usecases.UseCase
import usecases.accounts.dto.GetBufferInfoOutput
import usecases.accounts.dto.GetTotalBalanceAmountOutput
import usecases.accounts.dto.mapperAccountDetailOutput
import usecases.analystics.dto.ForcastSpendingInput
import usecases.analystics.dto.ForcastSpendingOutput
import usecases.invoices.dto.GetBalanceInput
import usecases.invoices.dto.GetBalanceOutput
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import kotlin.math.abs

class GetTotalBalanceAmount(
    private val accountRepo: IRepository<Account>,
    private val fundRepo: IRepository<Fund>,
    private val profile: IRepository<Profile>,
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    private val getBalance: UseCase<GetBalanceInput, GetBalanceOutput>,
    private val forcastSpending: UseCase<ForcastSpendingInput, ForcastSpendingOutput>,
): UseCase<Unit, GetTotalBalanceAmountOutput>() {
    override suspend fun process(input: Unit): GetTotalBalanceAmountOutput {
        val profile = profile.getAll(QueryFilter.queryAll()).items.firstOrNull() ?: return GetTotalBalanceAmountOutput(
            totalBalance = 0.0,
            totalAvailable = 0.0,
            totalFreeze = 0.0,
            totalLock = 0.0,
            totalCreditUtilization = 0.0,
            buffer = GetBufferInfoOutput(
                baseBufferAmount = 0.0,
                currentBalanceBuffer = 0.0,
                projectedBuffer = 0.0,
                projectedBufferByBalance = 0.0,
            )
        )

        val conditionAccount = QueryExtendBuilder<Account>()
            .addCondition("type", QueryComparator.In,
                listOf(AccountType.CHECKING, AccountType.BUSINESS, AccountType.CREDIT_CARD)
            )
        val accounts = accountRepo.getAll(QueryFilter.queryAll(), conditionAccount)
        val accountIds = accounts.items.map { it.id }

        val conditionFund = QueryExtendBuilder<Fund>()
            .addCondition("accountId", QueryComparator.In, accountIds)
        val funds = fundRepo.getAll(QueryFilter.queryAll(), conditionFund)

        val lockedBalance = funds.items.sumOf { it.balance }

        val freezeBalance = abs(getBalance.processDirect(GetBalanceInput(
            accountIds = accountIds.toSet(),
            isFreeze = true
        )).balance)

        val conditionScheduleInvoice = QueryExtendBuilder<ScheduleInvoice>()
            .addCondition("type", QueryComparator.Equal, InvoiceType.INCOME.value)
            .addCondition("scheduler.date", QueryComparator.GreaterOrEquals, LocalDateTime.now())
        val scheduleInvoices = scheduleInvoiceRepo.getAll(QueryFilter.queryAll(), conditionScheduleInvoice)
        val endDate = scheduleInvoices.items.maxOfOrNull { it.scheduler.date.toLocalDate() }
            ?: LocalDate.now().plusMonths(1).with(TemporalAdjusters.firstDayOfMonth())

        val totalBalanceAmount = accounts.items.sumOf { it.balance }

        val resForcast = forcastSpending.processDirect(ForcastSpendingInput(
            startDate = LocalDate.now(),
            endDate = endDate,
            wantItems = listOf(),
            savingAdditionalIncome = listOf(),
            budgetIds = listOf(),
            overrideAccountsBalance = totalBalanceAmount,
            savingRate = null
        ))

        val accountCredits = accounts.items.filter { it.detail.getType() == AccountType.CREDIT_CARD }
        val allCreditDetails = accountCredits.map { mapperAccountDetailOutput(it.detail, it.balance).detailForCreditCard }
        val totalCreditUtilization =  allCreditDetails.sumOf { it?.creditUtilisation ?: 0.0 }


        return GetTotalBalanceAmountOutput(
            totalBalance = totalBalanceAmount,
            totalAvailable = totalBalanceAmount + abs(freezeBalance + lockedBalance),
            totalFreeze = freezeBalance,
            totalLock = lockedBalance,
            totalCreditUtilization = totalCreditUtilization,
            buffer = GetBufferInfoOutput(
                baseBufferAmount = profile.balanceBuffer,
                currentBalanceBuffer = totalBalanceAmount - profile.balanceBuffer,
                projectedBuffer = resForcast.remainAmount,
                projectedBufferByBalance =  resForcast.remainAmount - profile.balanceBuffer,
            )
        )
    }
}