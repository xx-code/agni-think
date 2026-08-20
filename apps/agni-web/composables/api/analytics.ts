import type { ListResponse } from "~/types/api";
import type { BudgetTotalSummaryResponse, GetAnnualOutlookResponse, GetBudgetingRuleRequest, GetBudgetingRuleResponse, GetEstimationLeftAmountRequest, GetEstimationLeftAmountResponse, GetFundTotalSummary, GetSavingAnalysticRequest, GetSavingAnalysticResponse, GetSpendCategoryRequest, GetSpendCategoryResponse, GetSpendTagRequest, GetSpendTagResponse, PatrimonySummaryResponse } from "~/types/api/analytics";
import type { BudgetTotalSummary, EstimationLeftAmountType, PatrimonySummary, SavingAnalysticType } from "~/types/ui/analytics";

export async function fetchAnnualOutlook(): Promise<GetAnnualOutlookResponse>{
    const response = await $fetch<GetAnnualOutlookResponse>(`api/analytics/annual-outlook`, {
        method: 'GET'
    });

    return response 
}

export async function fetchBudgetingRule(request: GetBudgetingRuleRequest): Promise<GetBudgetingRuleResponse> {
    return await $fetch(`api/analytics/budgeting-rule`, {
        method: 'GET',
        query: request
    })
}


export async function fetchEstimationLeftAmount(request: GetEstimationLeftAmountRequest): Promise<EstimationLeftAmountType>{
    const response = await $fetch<GetEstimationLeftAmountResponse>(`api/analytics/estimation-left-amount`, {
        method: 'GET',
        query: request 
    });

    return { estimateAmount: response.estimateAmount };
}

export async function fetchAnalyticSavings(request: GetSavingAnalysticRequest): Promise<SavingAnalysticType>{
    const response = await $fetch<GetSavingAnalysticResponse>(`api/analytics/savings`, {
        method: 'GET',
        query: request 
    });

    return { 
        savings: response.savings,
        investments: response.investments,
        savingRates: response.savingRates,
        investementRates: response.investmentRates
    };
}


export async function fetchSpendByCategoriesAnalytic(request: GetSpendCategoryRequest): Promise<ListResponse<GetSpendCategoryResponse>> {
    const response = await $fetch<ListResponse<GetSpendCategoryResponse>>(`api/analytics/spend-categories`, {
        method: 'GET',
        query: request 
    });

    return response
}

export async function fetchSpendByTagAnalytic(request: GetSpendTagRequest): Promise<ListResponse<GetSpendTagResponse>> {
    const response = await $fetch<ListResponse<GetSpendTagResponse>>(`api/analytics/spend-tags`, {
        method: 'GET',
        query: request 
    });

    return response
}

export async function fetchFundSummary(): Promise<GetFundTotalSummary> {
    const res = await $fetch<GetFundTotalSummary>('/api/analytics/fund-total-summary', {
        method: 'GET'
    })

    return res
} 

export async function fetchBudgetTotalSummary(): Promise<BudgetTotalSummary> {
    const res = await $fetch<BudgetTotalSummaryResponse>('/api/analytics/budget-total-summary')
    return res
}

export async function fetchPatrimonySummary(): Promise<PatrimonySummary> {
    const res = await $fetch<PatrimonySummaryResponse>('/api/analytics/patrimony-summary')
    return res
}