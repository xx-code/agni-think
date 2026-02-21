package dev.auguste.agni_api.core.usecases.analystics

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.FinancePrinciple
import dev.auguste.agni_api.core.entities.IncomeSource
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.entities.enums.AccountType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.entities.roundTo
import dev.auguste.agni_api.core.usecases.analystics.dto.AccountInfoOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.ComingRevenueOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.ComingSpendingOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetFinanceProfileOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.IncomeSourceOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.PrincipeToFollowOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.value_objects.BrokingAccountDetail
import dev.auguste.agni_api.core.value_objects.BusinessAccountDetail
import dev.auguste.agni_api.core.value_objects.CheckingAccountDetail
import dev.auguste.agni_api.core.value_objects.CreditCardAccountDetail
import dev.auguste.agni_api.core.value_objects.SavingAccountDetail
import java.time.LocalDate
import kotlin.math.abs

class GetFinanceProfile(
    private val accountRepo: IRepository<Account>,
    private val principleRepo: IRepository<FinancePrinciple>,
    private val incomeSourceRepo: IRepository<IncomeSource>,
    private val scheduleInvoice: IRepository<ScheduleInvoice>
) : IUseCase<Unit, GetFinanceProfileOutput> {
    override fun execAsync(input: Unit): GetFinanceProfileOutput {
        val accounts = accountRepo.getAll(QueryFilter(0, 0, true))
        val principles = principleRepo.getAll(QueryFilter(0, 0, true))
        val incomes = incomeSourceRepo.getAll(QueryFilter(0, 0, true))
        val scheduleInvoices = scheduleInvoice.getAll(QueryFilter(0, 0, true))

        return GetFinanceProfileOutput(
            currentBalanceTotalWithFreeze = accounts.items.sumOf { it.balance },
            currentCreditUtilization = "${computeTotalCreditUtilization(accounts.items)}%"  ,
            accountInfos = formatAccounts(accounts.items),
            principles = formatFinancePrinciple(principles.items),
            comingSpending = formatComingSpend(scheduleInvoices.items),
            comingRevenue = formatComingRevenue(scheduleInvoices.items),
            incomeSources = formaIncomeSource(incomes.items)
        )

    }


    private fun computeTotalCreditUtilization(accounts: List<Account>): Double {
        var total = 0.0

        accounts.filter { it.detail.getType() == AccountType.CREDIT_CARD }.forEach {
            val detail = it.detail as CreditCardAccountDetail
            val utilization = if (detail.creditLimit > 0) {
                ((abs(it.balance) / detail.creditLimit).roundTo(2)) * 100
            } else {
                0.0
            }
            total += utilization
        }

        return total
    }

    private fun formaIncomeSource(incomeSource: List<IncomeSource>): List<IncomeSourceOutput> {
        val incomeSourceOutputs = mutableListOf<IncomeSourceOutput>()

        incomeSource.forEach { incomeSource ->
            incomeSourceOutputs.add(
                IncomeSourceOutput(
                    title = incomeSource.title,
                    type = incomeSource.type.value,
                    grossAmount = incomeSource.annualGrossAmount,
                    estimateNextNetAmount = incomeSource.getEstimateNextNetAmount(),
                    estimatedFutureOccurrences = incomeSource.getEstimateFutureOccurrence(),
                    confidence = incomeSource.reliabilityLevel
                )
            )
        }

        return incomeSourceOutputs
    }

    private fun formatComingRevenue(scheduleInvoices: List<ScheduleInvoice>) : List<ComingRevenueOutput> {
        val comingRevenueOutputs = mutableListOf<ComingRevenueOutput>()

        scheduleInvoices.filter { it.type == InvoiceType.INCOME }.forEach { scheduleInvoice ->
            comingRevenueOutputs.add(
                ComingRevenueOutput(
                    title = scheduleInvoice.title,
                    amount = scheduleInvoice.amount,
                    dueDate = scheduleInvoice.scheduler.date
                )
            )
        }

        return comingRevenueOutputs
    }


    private fun formatComingSpend(scheduleInvoices: List<ScheduleInvoice>) : List<ComingSpendingOutput> {
        val comingSpendOutputs = mutableListOf<ComingSpendingOutput>()

        scheduleInvoices.filter { it.type != InvoiceType.INCOME }.forEach { scheduleInvoice ->
            ComingSpendingOutput(
                title = scheduleInvoice.title,
                amount = scheduleInvoice.amount,
                dueDate = scheduleInvoice.scheduler.date
            )
        }

        return comingSpendOutputs
    }

    private fun formatFinancePrinciple(principes: List<FinancePrinciple>) : List<PrincipeToFollowOutput> {
        val financeOutputs = mutableListOf<PrincipeToFollowOutput>()

        principes.forEach { finance ->
            financeOutputs.add(
                PrincipeToFollowOutput(
                    title = finance.name,
                    ruleToFollow = finance.logicRules
                        ?: ("" + "Principe Target: ${finance.targetType}, Strictness: ${finance.strictness} 1 (soft) to 10 (hard)")
                )
            )
        }

        return financeOutputs
    }

    private fun formatAccounts(accounts: List<Account>): List<AccountInfoOutput> {
        val accountOutputs = mutableListOf<AccountInfoOutput>()

        accounts.forEach { account ->
            accountOutputs.add(AccountInfoOutput(
                accountName = account.title,
                accountType = account.detail.getType().value,
                balance = account.balance,
                accountDetailRule = when(account.detail.getType()) {
                    AccountType.CHECKING -> "I desir to have a buffer a ${(account.detail as CheckingAccountDetail).buffer}"
                    AccountType.CREDIT_CARD -> {
                        val now = LocalDate.now()
                        var nextPaymentDate = (account.detail as CreditCardAccountDetail).invoiceDate
                        while (nextPaymentDate.isBefore(now)) {
                            nextPaymentDate = nextPaymentDate.plusMonths(1)
                        }
                        "The Credit Card Limit is a ${(account.detail as CreditCardAccountDetail).creditLimit} the next Payment Invoice is $nextPaymentDate"
                    }
                    AccountType.SAVING -> "I desir to have holy threshold of ${(account.detail as SavingAccountDetail).secureAmount}"
                    AccountType.BUSINESS -> "I desir to have a buffer a ${(account.detail as BusinessAccountDetail).buffer}"
                    AccountType.BROKING -> "type management ${(account.detail as BrokingAccountDetail).managementType} contribution type ${(account.detail as BrokingAccountDetail).contributionType}"
                },
                isLiquidity = when(account.detail.getType()) {
                    AccountType.CHECKING -> true
                    AccountType.CREDIT_CARD -> true
                    else -> false
                }
            ))
        }

        return accountOutputs
    }
}