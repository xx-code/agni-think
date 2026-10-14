package usecase_configs

import adapters.IAuthenticateAdapter
import adapters.dto.FundSummaryOutput
import adapters.readers.IFundSummaryReader
import adapters.readers.IGetAllCategoryReader
import adapters.readers.IGetAllTagReader
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
import domain.entities.User
import org.springframework.context.annotation.Bean
import usecases.dto.ListOutput
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
import org.springframework.context.annotation.Configuration
import persistences.AccountRepository
import usecases.UseCase
import usecases.analystics.dto.FundSummaryInput
import usecases.patrimonies.dto.GetAllPatrimonyInput


@Configuration
class AnalyticConfig {

    @Bean
    fun getSavingBalance(
        accountRepository: AccountRepository,
        getBalance: UseCase<GetBalanceInput, GetBalanceOutput>,
        auth: IAuthenticateAdapter
    ): UseCase<GetSavingBalanceInput, Double> {
        return GetSavingBalance(
            accountRepo = accountRepository,
            getBalance = getBalance,
            auth = auth,
        )
    }

    @Bean
    fun getSpendCategoryAnalytic(
        categoryRepo: IGetAllCategoryReader,
        getBalanceByPeriod: IUseCase<GetBalancesByPeriodInput, List<GetBalanceByPeriodOutput>>,
        auth: IAuthenticateAdapter
    ) : UseCase<GetSpendByCategoryInput, ListOutput<GetSpendByCategoryOutput>> {
        return GetSpendByCategoryAnalytic(
            categoryRepo = categoryRepo,
            getBalanceByPeriod = getBalanceByPeriod,
            auth = auth
        )
    }

    @Bean
    fun getSpendTagAnalytic(
        tagRepo: IGetAllTagReader,
        getBalanceByPeriod: IUseCase<GetBalancesByPeriodInput, List<GetBalanceByPeriodOutput>>,
        auth: IAuthenticateAdapter
    ) : UseCase<GetSpendByTagInput, ListOutput<GetSpendByTagOutput>> {
        return GetSpendByTagAnalytic(
            tagRepo = tagRepo,
            getBalanceByPeriod = getBalanceByPeriod,
            auth = auth
        )
    }

    @Bean
    fun getSavingAnalytic(
        accountRepo: IRepository<Account>,
        getBalanceByPeriod: IUseCase<GetBalancesByPeriodInput, List<GetBalanceByPeriodOutput>>,
        auth: IAuthenticateAdapter
    ) : UseCase<GetSavingAnalyticInput, GetSavingAnalyticOutput> {
        return GetSavingAnalytic(
            accountRepo = accountRepo,
            getBalanceByPeriod = getBalanceByPeriod,
            auth = auth
        )
    }

    @Bean fun getFinancialProfile(
        accountRepo: IRepository<Account>,
        principleRepo: IRepository<FinancePrinciple>,
        incomeSourceRepo: IRepository<IncomeSource>,
        scheduleInvoice: IRepository<ScheduleInvoice>,
        auth: IAuthenticateAdapter
    ) : UseCase<Unit, GetFinanceProfileOutput> {
        return GetFinanceProfile(
            auth=auth,
            accountRepo = accountRepo,
            principleRepo = principleRepo,
            incomeSourceRepo = incomeSourceRepo,
            scheduleInvoice = scheduleInvoice
        )
    }

    @Bean fun getBudgetingRule(
        getSavingBalance: IUseCase<GetSavingBalanceInput, Double>,
        getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
        auth: IAuthenticateAdapter
    ) : UseCase<GetBudgetingRuleAnalyticInput, GetBudgetingRuleAnalyticOutput> {
        return GetBudgetingRuleAnalytic(
            getSavingBalance = getSavingBalance,
            getBalance = getBalance,
            auth = auth
        )
    }


    @Bean fun getAnnualOutlook(
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        categoryRepo: IRepository<Category>,
        getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
        getSavingBalance: IUseCase<GetSavingBalanceInput, Double>,
        getBudgets: IUseCase<GetAllBudgetInput, ListOutput<GetBudgetOutput>>,
        auth: IAuthenticateAdapter
    ) : UseCase<Unit, GetAnnualOutlookOutput>{
        return GetAnnualOutlook(
            scheduleRepo = scheduleInvoiceRepo,
            categoryRepo = categoryRepo,
            getBalance = getBalance,
            getBudgets = getBudgets,
            getSavingBalance = getSavingBalance,
            auth = auth
        )
    }

    @Bean fun getFundSummary(
        fundSummaryReader: IFundSummaryReader,
        auth: IAuthenticateAdapter
    ) : IUseCase<FundSummaryInput, FundSummaryOutput> {
        return GetFundTotalSummary(fundSummaryReader, auth)
    }

    @Bean fun getBudgetTotalSummary(
        repoBudget: IRepository<Budget>,
        getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
        auth: IAuthenticateAdapter
    ) : UseCase<Unit, GetBudgetTotalSummaryOutput> {
        return GetBudgetTotalSummary(
            auth = auth,
            repoBudget = repoBudget,
            getBalance = getBalance
        )
    }

    @Bean fun getPatrimonySummary(
        auth: IAuthenticateAdapter,
        getAllPatrimonies: IUseCase<GetAllPatrimonyInput, ListOutput<GetPatrimonyOutput>>
    ) : IUseCase<Unit, GetPatrimonySummaryOutput> {
        return GetPatrimonySummary(
            getAllPatrimonies = getAllPatrimonies,
            auth = auth
        )
    }


    @Bean
    fun getPatrimonyEvolution(
        patrimonyRepo: IRepository<Patrimony>,
        snapshotRepo: IRepository<PatrimonySnapshot>,
        fundRepo: IRepository<Fund>,
        auth: IAuthenticateAdapter,
        getBalanceByPeriod: IUseCase<GetBalancesByPeriodInput, List<GetBalanceByPeriodOutput>>
    ): UseCase<GetPatrimonyEvolutionInput, GetPatrimonyEvolutionOutput> {
        return GetPatrimonyEvolution(
            patrimonyRepo = patrimonyRepo,
            patrimonySnapshotRepo = snapshotRepo,
            getBalanceByPeriod = getBalanceByPeriod,
            fundRepo = fundRepo,
            auth = auth
        )
    }

    @Bean
    fun getProvisionSummary(
        auth: IAuthenticateAdapter,
        provisionRepo: IRepository<Provision> ) : IUseCase<Unit, GetProvisionSummaryOutput>
    {
        return GetProvisionSummary(provisionRepo, auth)
    }

    @Bean fun getScheduleInvoiceSummary(
        auth: IAuthenticateAdapter,
        invoiceSummaryRepo: IRepository<ScheduleInvoice>
    ) : UseCase<Unit, GetScheduleInvoiceSummaryOutput> {
        return GetScheduleInvoiceSummary(invoiceSummaryRepo, auth)
    }

    @Bean
    fun forcastSpending(
        scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
        auth: IAuthenticateAdapter,
        accountRepo: IRepository<Account>,
        budgetRepo: IRepository<Budget>,
        profileRepo: IRepository<Profile>,
        provisionRepo: IRepository<Provision>,
        fundRepo: IRepository<Fund>,
        getBalance: IUseCase<GetBalanceInput, GetBalanceOutput>,
    ) : UseCase<ForcastSpendingInput, ForcastSpendingOutput> {
        return ForcastSpending(
            scheduleInvoiceRepo = scheduleInvoiceRepo,
            accountRepo = accountRepo,
            budgetRepo = budgetRepo,
            profileRepo = profileRepo,
            getBalance = getBalance,
            provisionRepo = provisionRepo,
            fundRepo = fundRepo,
            auth = auth
        )
    }
}