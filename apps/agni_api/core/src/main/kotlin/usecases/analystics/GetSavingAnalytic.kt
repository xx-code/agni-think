package usecases.analystics

import adapters.IAuthenticateAdapter
import usecases.UseCase
import domain.SAVING_CATEGORY_ID
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.QueryComparator
import adapters.repositories.QueryExtendBuilder
import domain.entities.Account
import domain.entities.User
import domain.enums.AccountType
import domain.enums.InvoiceMovementType
import usecases.analystics.dto.GetSavingAnalyticInput
import usecases.analystics.dto.GetSavingAnalyticOutput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetBalanceByPeriodOutput
import usecases.invoices.dto.GetBalancesByPeriodInput

class GetSavingAnalytic(
    private val accountRepo: IRepository<Account>,
    private val getBalanceByPeriod: IUseCase<GetBalancesByPeriodInput, List<GetBalanceByPeriodOutput>>,
    auth: IAuthenticateAdapter
): UseCase<GetSavingAnalyticInput, GetSavingAnalyticOutput>(auth = auth) {

    override suspend fun process(input: GetSavingAnalyticInput): GetSavingAnalyticOutput {
        val currentUser = getCurrentUser()

        val condition = QueryExtendBuilder<Account>()
            .addCondition("userId", QueryComparator.Equal, currentUser.id)
        val accounts = accountRepo.getAll(QueryFilter.queryAll(), condition)
        val accountInvestmentIds = accounts.items
            .filter { it.detail.getType() == AccountType.BROKING }
            .map { it.id }
            .toSet()

        // 1. Entrées d'argent globales (Revenus)
        val balanceIncome = getBalanceByPeriod.processDirect(GetBalancesByPeriodInput(
            userId = currentUser.id,
            period = input.period,
            interval = input.interval,
            dateFrom = input.startDate,
            movement = InvoiceMovementType.CREDIT
        ))

        // 2. Épargne explicite : Uniquement les dépenses avec le Tag/Catégorie Épargne
        val balanceSavingCategory = getBalanceByPeriod.processDirect(GetBalancesByPeriodInput(
            userId = currentUser.id,
            period = input.period,
            interval = input.interval,
            dateFrom = input.startDate,
            categoryIds = setOf(SAVING_CATEGORY_ID)
        ))

        // 3. Investissement : Uniquement l'argent qui entre sur les comptes de Brokage
        val balanceInvestmentAccount = getBalanceByPeriod.processDirect(GetBalancesByPeriodInput(
            userId = currentUser.id,
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