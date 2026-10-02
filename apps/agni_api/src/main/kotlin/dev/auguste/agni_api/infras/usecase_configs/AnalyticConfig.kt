package dev.auguste.agni_api.infras.usecase_configs

import adapters.dto.FundSummaryOutput
import adapters.dto.QueryFilter
import adapters.readers.IFundSummaryReader
import adapters.repositories.IRepository
import domain.entities.Account
import domain.entities.Budget
import domain.entities.Category
import domain.entities.FinancePrinciple
import domain.entities.IncomeSource
import domain.entities.Patrimony
import domain.entities.PatrimonySnapshot
import domain.entities.Profile
import domain.entities.Provision
import domain.entities.Fund
import domain.entities.ScheduleInvoice
import domain.entities.Tag
import usecases.ListOutput
import usecases.analystics.ForcastSpending
import usecases.analystics.GetAnnualOutlook
import usecases.analystics.GetBudgetTotalSummary
import usecases.analystics.GetBudgetingRuleAnalytic
import usecases.analystics.GetFinanceProfile
import usecases.analystics.GetFundTotalSummary
import usecases.analystics.GetPatrimonyEvolution
import usecases.analystics.GetPatrimonySummary
import usecases.analystics.GetProvisionSummary
import usecases.analystics.GetSavingAnalytic
import usecases.analystics.GetSavingBalance
import usecases.analystics.GetScheduleInvoiceSummary
import usecases.analystics.GetSpendByCategoryAnalytic
import usecases.analystics.GetSpendByTagAnalytic
import usecases.analystics.dto.ForcastSpendingInput
import usecases.analystics.dto.ForcastSpendingOutput
import usecases.analystics.dto.GetAnnualOutlookOutput
import usecases.analystics.dto.GetBudgetTotalSummaryOutput
import usecases.analystics.dto.GetBudgetingRuleAnalyticInput
import usecases.analystics.dto.GetBudgetingRuleAnalyticOutput
import usecases.analystics.dto.GetFinanceProfileOutput
import usecases.analystics.dto.GetPatrimonyEvolutionInput
import usecases.analystics.dto.GetPatrimonyEvolutionOutput
import usecases.analystics.dto.GetPatrimonySummaryOutput
import usecases.analystics.dto.GetProvisionSummaryOutput
import usecases.analystics.dto.GetSavingAnalyticInput
import usecases.analystics.dto.GetSavingAnalyticOutput
import usecases.analystics.dto.GetSavingBalanceInput
import usecases.analystics.dto.GetScheduleInvoiceSummaryOutput
import usecases.analystics.dto.GetSpendByCategoryInput
import usecases.analystics.dto.GetSpendByCategoryOutput
import usecases.analystics.dto.GetSpendByTagInput
import usecases.analystics.dto.GetSpendByTagOutput
import usecases.budgets.dto.GetAllBudgetInput
import usecases.budgets.dto.GetBudgetOutput
import usecases.interfaces.IUseCase
import usecases.invoices.dto.GetBalanceByPeriodOutput
import usecases.invoices.dto.GetBalanceInput
import usecases.invoices.dto.GetBalanceOutput
import usecases.invoices.dto.GetBalancesByPeriodInput
import usecases.patrimonies.dto.GetPatrimonyOutput
import dev.auguste.agni_api.infras.persistences.AccountRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class AnalyticConfig {

    @Bean fun getSavingBalance(
        accountRepository: AccountRepository,
        getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
    ): IUseCase<GetSavingBalanceInput, Double> {
        return GetSavingBalance(
            accountRepo = accountRepository,
            getBalance = getBalance
        )
    }

    @Bean
    fun getSpendCategoryAnalytic(
        categoryRepo: IRepository<Category>,
        getBalanceByPeriod: IUseCase<GetBalancesByPeriodInput, List<GetBalanceByPeriodOutput>>,
    ) : IUseCase<GetSpendByCategoryInput, ListOutput<GetSpendByCategoryOutput>> {
        return GetSpendByCategoryAnalytic(
            categoryRepo = categoryRepo,
            getBalanceByPeriod = getBalanceByPeriod
        )
    }

    @Bean
    fun getSpendTagAnalytic(
        tagRepo: IRepository<Tag>,
        getBalanceByPeriod: IUseCase<GetBalancesByPeriodInput, List<GetBalanceByPeriodOutput>>,
    ) : IUseCase<GetSpendByTagInput, ListOutput<GetSpendByTagOutput>> {
        return GetSpendByTagAnalytic(
            tagRepo = tagRepo,
            getBalanceByPeriod = getBalanceByPeriod
        )
    }

    @Bean
    fun getSavingAnalytic(
        accountRepo: IRepository<Account>,
        getBalanceByPeriod: IUseCase<GetBalancesByPeriodInput, List<GetBalanceByPeriodOutput>>,
    ) : IUseCase<GetSavingAnalyticInput, GetSavingAnalyticOutput> {
        return GetSavingAnalytic(
            accountRepo = accountRepo,
            getBalanceByPeriod = getBalanceByPeriod
        )
    }

    @Bean fun getFinancialProfile(
        accountRepo: IRepository<Account>,
        principleRepo: IRepository<FinancePrinciple>,
        incomeSourceRepo: IRepository<IncomeSource>,
        scheduleInvoice: IRepository<ScheduleInvoice>
    ) : IUseCase<Unit, GetFinanceProfileOutput> {
        return GetFinanceProfile(
            accountRepo = accountRepo,
            principleRepo = principleRepo,
            incomeSourceRepo = incomeSourceRepo,
            scheduleInvoice = scheduleInvoice
        )
    }

    @Bean fun getBudgetingRule(
        getSavingBalance: IUseCase<GetSavingBalanceInput, Double>,
        getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
    ) : IUseCase<GetBudgetingRuleAnalyticInput, GetBudgetingRuleAnalyticOutput> {
        return GetBudgetingRuleAnalytic(
            getSavingBalance = getSavingBalance,
            getBalance = getBalance
        )
    }


    @Bean fun getAnnualOutlook(
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        categoryRepo: IRepository<Category>,
        getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
        getSavingBalance: IUseCase<GetSavingBalanceInput, Double>,
        getBudgets: IUseCase<GetAllBudgetInput, ListOutput<GetBudgetOutput>>
    ) : IUseCase<Unit, GetAnnualOutlookOutput>{
        return GetAnnualOutlook(
            scheduleRepo = scheduleInvoiceRepo,
            categoryRepo = categoryRepo,
            getBalance = getBalance,
            getBudgets = getBudgets,
            getSavingBalance = getSavingBalance
        )
    }

    @Bean fun getFundSummary(
        fundSummaryReader: IFundSummaryReader
    ) : IUseCase<Unit, FundSummaryOutput> {
        return GetFundTotalSummary(fundSummaryReader)
    }

    @Bean fun getBudgetTotalSummary(
        repoBudget: IRepository<Budget>,
        getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
    ) : IUseCase<Unit, GetBudgetTotalSummaryOutput> {
        return GetBudgetTotalSummary(
            repoBudget = repoBudget,
            getBalance = getBalance
        )
    }

    @Bean fun getPatrimonySummary(
        getAllPatrimonies: IUseCase<QueryFilter, ListOutput<GetPatrimonyOutput>>
    ) : IUseCase<Unit, GetPatrimonySummaryOutput> {
        return GetPatrimonySummary(
            getAllPatrimonies = getAllPatrimonies,
        )
    }


    @Bean
    fun getPatrimonyEvolution(
        patrimonyRepo: IRepository<Patrimony>,
        snapshotRepo: IRepository<PatrimonySnapshot>,
        fundRepo: IRepository<Fund>,
        getBalanceByPeriod: IUseCase<GetBalancesByPeriodInput, List<GetBalanceByPeriodOutput>>
    ): IUseCase<GetPatrimonyEvolutionInput, GetPatrimonyEvolutionOutput> {
        return GetPatrimonyEvolution(
            patrimonyRepo = patrimonyRepo,
            patrimonySnapshotRepo = snapshotRepo,
            getBalanceByPeriod = getBalanceByPeriod,
            fundRepo = fundRepo,
        )
    }

    @Bean
    fun getProvisionSummary(
        provisionRepo: IRepository<Provision> ) : IUseCase<Unit, GetProvisionSummaryOutput>
    {
        return GetProvisionSummary(provisionRepo)
    }

    @Bean fun getScheduleInvoiceSummary(
        invoiceSummaryRepo: IRepository<ScheduleInvoice>
    ) : IUseCase<Unit, GetScheduleInvoiceSummaryOutput> {
        return GetScheduleInvoiceSummary(invoiceSummaryRepo)
    }

    @Bean
    fun forcastSpending(
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        accountRepo: IRepository<Account>,
        budgetRepo: IRepository<Budget>,
        profileRepo: IRepository<Profile>,
        provisionRepo: IRepository<Provision>,
        fundRepo: IRepository<Fund>,
        getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
    ) : IUseCase<ForcastSpendingInput, ForcastSpendingOutput> {
        return ForcastSpending(
            scheduleInvoiceRepo = scheduleInvoiceRepo,
            accountRepo = accountRepo,
            budgetRepo = budgetRepo,
            profileRepo = profileRepo,
            getBalance = getBalance,
            provisionRepo = provisionRepo,
            fundRepo = fundRepo,
        )
    }
}