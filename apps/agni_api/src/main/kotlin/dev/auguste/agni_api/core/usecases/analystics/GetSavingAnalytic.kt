package dev.auguste.agni_api.core.usecases.analystics

import dev.auguste.agni_api.core.SAVING_CATEGORY_ID
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Account
import dev.auguste.agni_api.core.entities.enums.AccountType
import dev.auguste.agni_api.core.entities.enums.InvoiceMouvementType
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSavingAnalyticInput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSavingAnalyticOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalancesByPeriodInput

class GetSavingAnalytic(
    private val accountRepo: IRepository<Account>,
    private val getBalanceByPeriod : IUseCase<GetBalancesByPeriodInput, List<GetBalanceOutput>>
) : IUseCase<GetSavingAnalyticInput, GetSavingAnalyticOutput> {
    override fun execAsync(input: GetSavingAnalyticInput): GetSavingAnalyticOutput {

        val accounts = accountRepo.getAll(QueryFilter(0, 0, true))
        val accountSavingIds = accounts.items.filter { it.detail.getType() == AccountType.SAVING }
        val accountInvestmentIds = accounts.items.filter { it.detail.getType() == AccountType.BROKING }

        val balanceIncome = getBalanceByPeriod.execAsync(GetBalancesByPeriodInput(
            period = input.period,
            interval = input.interval,
            dateFrom = input.startDate,
            mouvement = InvoiceMouvementType.CREDIT
        ))

        // Saving
        val balanceSaving = getBalanceByPeriod.execAsync(GetBalancesByPeriodInput(
            period = input.period,
            interval = input.interval,
            dateFrom = input.startDate,
            categoryIds = setOf(SAVING_CATEGORY_ID)
        ))

        val balanceSavingAccount = getBalanceByPeriod.execAsync(GetBalancesByPeriodInput(
            accountIds = accountSavingIds.map { it.id }.toSet(),
            period = input.period,
            interval = input.interval,
            dateFrom = input.startDate,
            removeSystemCategory = false
        ))

        val balanceInvestment = getBalanceByPeriod.execAsync(GetBalancesByPeriodInput(
            accountIds = accountInvestmentIds.map { it.id }.toSet(),
            period = input.period,
            interval = input.interval,
            dateFrom = input.startDate,
            removeSystemCategory = false
        ))

        val totalSavings = mutableListOf<Double>()
        balanceSaving.forEachIndexed { index, output ->
            totalSavings.add(output.spend + balanceSavingAccount[index].income + balanceInvestment[index].income)
        }
        //

        val investingRate = mutableListOf<Double>()
        val savingRate = mutableListOf<Double>()
        balanceIncome.forEachIndexed { index, output ->
            val income = output.income
            val invSpend = balanceInvestment[index].spend
            val savSpend = balanceSaving[index].spend

            if (income > 0) {
                // Handle Investment Rate
                if (invSpend > 0) {
                    investingRate.add(invSpend / income)
                } else {
                    investingRate.add(0.0)
                }

                // Handle Saving Rate
                if (savSpend > 0) {
                    savingRate.add(savSpend / income)
                } else {
                    savingRate.add(0.0)
                }
            } else {
                investingRate.add(0.0)
                savingRate.add(0.0)
            }
        }

        return GetSavingAnalyticOutput(
            savings = balanceSaving.map { it.spend },
            investments = balanceInvestment.map { it.spend },
            savingRates = savingRate,
            investmentRate = investingRate,
        )
    }
}