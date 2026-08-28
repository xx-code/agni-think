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
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceByPeriodOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalanceOutput
import dev.auguste.agni_api.core.usecases.invoices.dto.GetBalancesByPeriodInput

class GetSavingAnalytic(
    private val accountRepo: IRepository<Account>,
    private val getBalanceByPeriod: IUseCase<GetBalancesByPeriodInput, List<GetBalanceByPeriodOutput>>
) : IUseCase<GetSavingAnalyticInput, GetSavingAnalyticOutput> {

    override fun execAsync(input: GetSavingAnalyticInput): GetSavingAnalyticOutput {

        val accounts = accountRepo.getAll(QueryFilter(0, 0, true))
        val accountInvestmentIds = accounts.items
            .filter { it.detail.getType() == AccountType.BROKING }
            .map { it.id }
            .toSet()

        // 1. Entrées d'argent globales (Revenus)
        val balanceIncome = getBalanceByPeriod.execAsync(GetBalancesByPeriodInput(
            period = input.period,
            interval = input.interval,
            dateFrom = input.startDate,
            mouvement = InvoiceMouvementType.CREDIT
        ))

        // 2. Épargne explicite : Uniquement les dépenses avec le Tag/Catégorie Épargne
        val balanceSavingCategory = getBalanceByPeriod.execAsync(GetBalancesByPeriodInput(
            period = input.period,
            interval = input.interval,
            dateFrom = input.startDate,
            categoryIds = setOf(SAVING_CATEGORY_ID)
        ))

        // 3. Investissement : Uniquement l'argent qui entre sur les comptes de Brokage
        val balanceInvestmentAccount = getBalanceByPeriod.execAsync(GetBalancesByPeriodInput(
            accountIds = accountInvestmentIds,
            period = input.period,
            interval = input.interval,
            dateFrom = input.startDate,
            removeSystemCategory = false
        ))

        val savingsList = mutableListOf<Double>()
        val investmentsList = mutableListOf<Double>()
        val investingRates = mutableListOf<Double>()
        val savingRates = mutableListOf<Double>()

        balanceIncome.forEachIndexed { index, incomeOutput ->
            val income = incomeOutput.income

            // Seules les actions explicites sont comptabilisées
            val savingEffort = balanceSavingCategory[index].spend
            val investmentEffort = balanceInvestmentAccount[index].income

            savingsList.add(savingEffort)
            investmentsList.add(investmentEffort)

            if (income > 0) {
                savingRates.add(savingEffort / income)
                investingRates.add(investmentEffort / income)
            } else {
                savingRates.add(0.0)
                investingRates.add(0.0)
            }
        }

        return GetSavingAnalyticOutput(
            savings = savingsList,
            investments = investmentsList,
            savingRates = savingRates,
            investmentRate = investingRates
        )
    }
}