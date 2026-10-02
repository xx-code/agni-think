package dev.auguste.agni_api.controllers

import dev.auguste.agni_api.controllers.models.ApiForcastSpendingModel
import dev.auguste.agni_api.controllers.models.ApiGetBudgetingRuleModel
import dev.auguste.agni_api.controllers.models.ApiGetCategoryAnalyticModel
import dev.auguste.agni_api.controllers.models.ApiGetPatrimonyEvolutionModel
import dev.auguste.agni_api.controllers.models.ApiGetSavingAnalyticModel
import dev.auguste.agni_api.controllers.models.ApiGetTagAnalyticModel
import adapters.dto.FundSummaryOutput
import adapters.dto.QueryFilter
import domain.enums.PeriodType
import usecases.ListOutput
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
import usecases.analystics.dto.GetScheduleInvoiceSummaryOutput
import usecases.analystics.dto.GetSpendByCategoryInput
import usecases.analystics.dto.GetSpendByCategoryOutput
import usecases.analystics.dto.GetSpendByTagInput
import usecases.analystics.dto.GetSpendByTagOutput
import usecases.analystics.dto.SavingAdditionalIncomeInput
import usecases.analystics.dto.WantItemOutput
import usecases.interfaces.IUseCase
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v2/analytics")
class AnalyticController(
    private val getSpendTagAnalytic: IUseCase<GetSpendByTagInput, ListOutput<GetSpendByTagOutput>>,
    private val getSpendCategoryAnalytic: IUseCase<GetSpendByCategoryInput, ListOutput<GetSpendByCategoryOutput>>,
    private val getSavingAnalytic: IUseCase<GetSavingAnalyticInput, GetSavingAnalyticOutput>,
    private val getFinanceProfile: IUseCase<Unit, GetFinanceProfileOutput>,
    private val getBudgetingRuleAnalytic: IUseCase<GetBudgetingRuleAnalyticInput, GetBudgetingRuleAnalyticOutput>,
    private val getAnnualOutlook: IUseCase<Unit, GetAnnualOutlookOutput>,
    private val getFundTotalSummary: IUseCase<Unit, FundSummaryOutput>,
    private val getBudgetTotalSummary: IUseCase<Unit, GetBudgetTotalSummaryOutput>,
    private val getPatrimonySummary: IUseCase<Unit, GetPatrimonySummaryOutput>,
    private val getPatrimonyEvolution: IUseCase<GetPatrimonyEvolutionInput, GetPatrimonyEvolutionOutput>,
    private val getProvisionSummary: IUseCase<Unit, GetProvisionSummaryOutput>,
    private val getScheduleInvoiceSummary: IUseCase<Unit, GetScheduleInvoiceSummaryOutput>,
    private val forcastSpending: IUseCase<ForcastSpendingInput, ForcastSpendingOutput>,
) {
    @GetMapping("/spend-categories")
    fun getSpendCategoriesAnalytic(query: ApiGetCategoryAnalyticModel) : ResponseEntity<ListOutput<GetSpendByCategoryOutput>> {
        return ResponseEntity.ok(getSpendCategoryAnalytic.execAsync(
            GetSpendByCategoryInput(
                period = PeriodType.fromString(query.period),
                interval = query.interval,
                startDate = query.startDate,
                query = QueryFilter(
                    query.offset,
                    query.limit,
                    query.queryAll
                )
            )
        ))
    }

    @GetMapping("/spend-tags")
    fun getSpendTagsAnalytic(query: ApiGetTagAnalyticModel) : ResponseEntity<ListOutput<GetSpendByTagOutput>> {
        return ResponseEntity.ok(getSpendTagAnalytic.execAsync(
            GetSpendByTagInput(
                period = PeriodType.fromString(query.period),
                interval = query.interval,
                startDate = query.startDate,
                query = QueryFilter(
                    query.offset,
                    query.limit,
                    query.queryAll
                ),
                categoryId = query.categoryId
            )
        ))
    }

    @GetMapping("/savings")
    fun getSavingAnalytic(query: ApiGetSavingAnalyticModel) : ResponseEntity<GetSavingAnalyticOutput> {
        return ResponseEntity.ok(getSavingAnalytic.execAsync(
            GetSavingAnalyticInput(
                period = PeriodType.fromString(query.period),
                interval = query.interval,
                startDate = query.startDate
            )
        ))
    }

    @GetMapping("/finance-profile")
    fun getFinanceProfile() : ResponseEntity<GetFinanceProfileOutput> {
        return ResponseEntity.ok(getFinanceProfile.execAsync(Unit))
    }

    @GetMapping("/budgeting-rule")
    fun getBudgetingRuleAnalyse(query: ApiGetBudgetingRuleModel) : ResponseEntity<GetBudgetingRuleAnalyticOutput> {
        return ResponseEntity.ok(getBudgetingRuleAnalytic.execAsync(
            GetBudgetingRuleAnalyticInput(
                period = query.period?.let { PeriodType.fromString(it) },
                interval = query.interval,
                startDate = query.startDate,
                endDate = query.endDate
            )
        ))
    }

    @GetMapping("/annual-outlook")
    fun getAnnualOutlook() : ResponseEntity<GetAnnualOutlookOutput> {
        return ResponseEntity.ok(getAnnualOutlook.execAsync(Unit))
    }

    @GetMapping("/fund-total-summary")
    fun getFundSummary() : ResponseEntity<FundSummaryOutput> {
        return ResponseEntity.ok(getFundTotalSummary.execAsync(Unit))
    }

    @GetMapping("/budget-total-summary")
    fun getBudgetTotalSummary() : ResponseEntity<GetBudgetTotalSummaryOutput> {
        return ResponseEntity.ok(getBudgetTotalSummary.execAsync(Unit))
    }

    @GetMapping("/patrimony-summary")
    fun getPatrimonySummary() : ResponseEntity<GetPatrimonySummaryOutput> {
        return ResponseEntity.ok(getPatrimonySummary.execAsync(Unit))
    }

    @GetMapping("/patrimony-evolution")
    fun getPatrimonyEvolution(query: ApiGetPatrimonyEvolutionModel) : ResponseEntity<GetPatrimonyEvolutionOutput> {
        return ResponseEntity.ok(getPatrimonyEvolution.execAsync(GetPatrimonyEvolutionInput(
            PeriodType.fromString(query.period),
            query.interval
        )))
    }

    @GetMapping("/provision-summary")
    fun getProvisionSummary() : ResponseEntity<GetProvisionSummaryOutput> {
        return ResponseEntity.ok(getProvisionSummary.execAsync(Unit))
    }

    @GetMapping("/schedule-invoice-summary")
    fun getScheduleInvoiceSummary() : ResponseEntity<GetScheduleInvoiceSummaryOutput> {
        return ResponseEntity.ok(getScheduleInvoiceSummary.execAsync(Unit))
    }

    @PostMapping("/forcast-spending")
    private fun forcastSpending(@Valid @RequestBody input: ApiForcastSpendingModel): ResponseEntity<ForcastSpendingOutput>{
        return ResponseEntity.ok(forcastSpending.execAsync(
            input = ForcastSpendingInput(
                startDate = input.startDate,
                endDate = input.endDate,
                wantItems = input.wantItems.map {
                    WantItemOutput(
                        description = it.description,
                        amount = it.amount
                    )
                },
                savingAdditionalIncome = input.savingAdditionalIncome.map {
                    SavingAdditionalIncomeInput(
                        savingAccountId = it.savingAccountId,
                        amount = it.amount
                    )
                },
                overrideAccountsBalance = input.overrideAccountsBalance,
                budgetIds = input.budgetIds,
                savingRate = input.savingRate
            )
        ))
    }
}