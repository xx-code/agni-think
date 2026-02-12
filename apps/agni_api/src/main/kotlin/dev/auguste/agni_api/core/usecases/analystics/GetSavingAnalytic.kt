package dev.auguste.agni_api.core.usecases.analystics

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.entities.enums.AccountType
import dev.auguste.agni_api.core.entities.enums.InvoiceMouvementType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.usecases.analystics.dto.GetAnalysticInput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSavingAnalyticOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalancesByPeriodInput

class GetSavingAnalytic(
    private val accountRepo: IRepository<Account>,
    private val getBalanceByPeriod : IUseCase<GetBalancesByPeriodInput, List<GetBalanceOutput>>
) : IUseCase<GetAnalysticInput, GetSavingAnalyticOutput> {
    override fun execAsync(input: GetAnalysticInput): GetSavingAnalyticOutput {

        val accounts = accountRepo.getAll(QueryFilter(0, 0, true))
        val accountSavingIds = accounts.items.filter { it.detail.getType() == AccountType.SAVING }
        val accountInvestmentIds = accounts.items.filter { it.detail.getType() == AccountType.BROKING }

        val balanceIncome = getBalanceByPeriod.execAsync(GetBalancesByPeriodInput(
            period = input.period,
            interval = input.interval,
            dateFrom = input.startDate,
            mouvement = InvoiceMouvementType.CREDIT
        ))

        // Rework
        val balanceSaving = getBalanceByPeriod.execAsync(GetBalancesByPeriodInput(
            accountIds = accountSavingIds.map { it.id }.toSet() + accountInvestmentIds.map { it.id },
            period = input.period,
            interval = input.interval,
            dateFrom = input.startDate,
            types = setOf(InvoiceType.INCOME, InvoiceType.OTHER),
            mouvement = InvoiceMouvementType.CREDIT
        ))

        val balanceInvestment = getBalanceByPeriod.execAsync(GetBalancesByPeriodInput(
            accountIds = accountInvestmentIds.map { it.id }.toSet(),
            period = input.period,
            interval = input.interval,
            dateFrom = input.startDate,
            mouvement = InvoiceMouvementType.CREDIT
        ))

        val investingRate = mutableSetOf<Double>()
        val savingRate = mutableSetOf<Double>()
        balanceIncome.forEachIndexed { index, output ->
            if (output.income <= 0) {
                investingRate.add(balanceInvestment[index].balance/output.balance)
                savingRate.add(balanceSaving[index].balance/output.balance)
            } else {
                investingRate.add(0.0)
                savingRate.add(0.0)
            }
        }

        return GetSavingAnalyticOutput(
            savings = balanceSaving.map { it.balance },
            investments = balanceInvestment.map { it.balance },
            savingRates = savingRate.toList(),
            investmentRate = investingRate.toList(),
        )
    }
}