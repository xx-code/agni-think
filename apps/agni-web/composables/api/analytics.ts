import { estimationLeftAmountResponseToEstimationLeftAmount, patrimonyEvolutionResponseToPatrimonyEvolution, savingAnalyticResponseToSavingAnalytic } from "~/mappers/analytics";
import type { ListResponse } from "~/types/api";
import type { BudgetTotalSummaryResponse, GetAnnualOutlookResponse, GetBudgetingRuleRequest, GetBudgetingRuleResponse, GetEstimationLeftAmountRequest, GetEstimationLeftAmountResponse, GetFundTotalSummary, GetSavingAnalysticRequest, GetSavingAnalysticResponse, GetSpendCategoryRequest, GetSpendCategoryResponse, GetSpendTagRequest, GetSpendTagResponse, PatrimonySummaryResponse, QueryPatrimonyEvolution } from "~/types/api/analytics";
import type { BudgetTotalSummary, EstimationLeftAmountType, PatrimonyEvolution, PatrimonySummary, SavingAnalysticType } from "~/types/ui/analytics";
import { ApiLinkBuilder } from "~/utils/ApiLinkBuilder";
import { API_ROUTES } from "~/shared/routes";

export async function fetchAnnualOutlook(): Promise<GetAnnualOutlookResponse>{
    return await ApiLinkBuilder
        .route<GetAnnualOutlookResponse>(API_ROUTES.ANALYTICS.ANNUAL_OUTLOOK)
        .execute()
}

export async function fetchBudgetingRule(request: GetBudgetingRuleRequest): Promise<GetBudgetingRuleResponse> {
    return await ApiLinkBuilder
        .route<GetBudgetingRuleResponse>(API_ROUTES.ANALYTICS.BUDGETING_RULE)
        .query(request)
        .execute()
}

export async function fetchEstimationLeftAmount(request: GetEstimationLeftAmountRequest): Promise<EstimationLeftAmountType>{
    return await ApiLinkBuilder
        .route<GetEstimationLeftAmountResponse>(API_ROUTES.ANALYTICS.ESTIMATION_LEFT_AMOUNT)
        .query(request)
        .mapper(estimationLeftAmountResponseToEstimationLeftAmount)
        .execute()
}

export async function fetchAnalyticSavings(request: GetSavingAnalysticRequest): Promise<SavingAnalysticType>{
    return await ApiLinkBuilder
        .route<GetSavingAnalysticResponse>(API_ROUTES.ANALYTICS.SAVINGS)
        .query(request)
        .mapper(savingAnalyticResponseToSavingAnalytic)
        .execute()
}

export async function fetchSpendByCategoriesAnalytic(request: GetSpendCategoryRequest): Promise<ListResponse<GetSpendCategoryResponse>> {
    return await ApiLinkBuilder
        .route<ListResponse<GetSpendCategoryResponse>>(API_ROUTES.ANALYTICS.SPEND_CATEGORIES)
        .query(request)
        .execute()
}

export async function fetchSpendByTagAnalytic(request: GetSpendTagRequest): Promise<ListResponse<GetSpendTagResponse>> {
    return await ApiLinkBuilder
        .route<ListResponse<GetSpendTagResponse>>(API_ROUTES.ANALYTICS.SPEND_TAGS)
        .query(request)
        .execute()
}

export async function fetchFundSummary(): Promise<GetFundTotalSummary> {
    return await ApiLinkBuilder
        .route<GetFundTotalSummary>(API_ROUTES.ANALYTICS.FUND_TOTAL_SUMMARY)
        .execute()
}

export async function fetchBudgetTotalSummary(): Promise<BudgetTotalSummary> {
    return await ApiLinkBuilder
        .route<BudgetTotalSummaryResponse>(API_ROUTES.ANALYTICS.BUDGET_TOTAL_SUMMARY)
        .execute()
}

export async function fetchPatrimonySummary(): Promise<PatrimonySummary> {
    return await ApiLinkBuilder
        .route<PatrimonySummaryResponse>(API_ROUTES.ANALYTICS.PATRIMONY_SUMMARY)
        .execute()
}

export async function fetchPatrimonyEvolution(query: QueryPatrimonyEvolution): Promise<PatrimonyEvolution> {
    return await ApiLinkBuilder
        .route(API_ROUTES.ANALYTICS.PATRIMONY_EVOLUTION)
        .query(query)
        .mapper(patrimonyEvolutionResponseToPatrimonyEvolution)
        .execute()
}
