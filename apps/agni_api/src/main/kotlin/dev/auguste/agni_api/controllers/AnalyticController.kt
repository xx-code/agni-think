package dev.auguste.agni_api.controllers

import dev.auguste.agni_api.controllers.models.ApiGetBudgetingRuleModel
import dev.auguste.agni_api.controllers.models.ApiGetCategoryAnalyticModel
import dev.auguste.agni_api.controllers.models.ApiGetPatrimonyEvolutionModel
import dev.auguste.agni_api.controllers.models.ApiGetSavingAnalyticModel
import dev.auguste.agni_api.controllers.models.ApiGetTagAnalyticModel
import dev.auguste.agni_api.core.adapters.dto.FundSummaryOutput
import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.entities.enums.PeriodType
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetAnnualOutlookOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetBudgetTotalSummaryOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetBudgetingRuleAnalyticInput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetBudgetingRuleAnalyticOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetFinanceProfileOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetPatrimonyEvolutionInput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetPatrimonyEvolutionOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetPatrimonySummaryOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetProvisionSummaryOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSavingAnalyticInput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSavingAnalyticOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetScheduleInvoiceSummaryOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSpendByCategoryInput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSpendByCategoryOutput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSpendByTagInput
import dev.auguste.agni_api.core.usecases.analystics.dto.GetSpendByTagOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
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
    private val getScheduleInvoiceSummary: IUseCase<Unit, GetScheduleInvoiceSummaryOutput>
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
}