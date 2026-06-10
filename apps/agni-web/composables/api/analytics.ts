import type { ListResponse } from "~/types/api";
import type { GetAnnualOutlookResponse, GetBudgetingRuleRequest, GetBudgetingRuleResponse, GetEstimationLeftAmountRequest, GetEstimationLeftAmountResponse, GetSavingAnalysticRequest, GetSavingAnalysticResponse, GetSpendCategoryRequest, GetSpendCategoryResponse, GetSpendTagRequest, GetSpendTagResponse } from "~/types/api/analytics";
import type { EstimationLeftAmountType, SavingAnalysticType } from "~/types/ui/analytics";

export async function fetchAnnualOutlook(): Promise<GetAnnualOutlookResponse>{
    const response = await $fetch<GetAnnualOutlookResponse>(`${getApiBase()}/analytics/annual-outlook`, {
        method: 'GET'
    });

    return response 
}

export async function fetchBudgetingRule(request: GetBudgetingRuleRequest): Promise<GetBudgetingRuleResponse> {
    return await $fetch(`${getApiBase()}/analytics/budgeting-rule`, {
        method: 'GET',
        query: request
    })
}


export async function fetchEstimationLeftAmount(request: GetEstimationLeftAmountRequest): Promise<EstimationLeftAmountType>{
    const response = await $fetch<GetEstimationLeftAmountResponse>(`${getApiBase()}/analytics/estimation-left-amount`, {
        method: 'GET',
        query: request 
    });

    return { estimateAmount: response.estimateAmount };
}

export async function fetchAnalyticSavings(request: GetSavingAnalysticRequest): Promise<SavingAnalysticType>{
    const response = await $fetch<GetSavingAnalysticResponse>(`${getApiBase()}/analytics/savings`, {
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
    const response = await $fetch<ListResponse<GetSpendCategoryResponse>>(`${getApiBase()}/analytics/spend-categories`, {
        method: 'GET',
        query: request 
    });

    return response
}

export async function fetchSpendByTagAnalytic(request: GetSpendTagRequest): Promise<ListResponse<GetSpendTagResponse>> {
    const response = await $fetch<ListResponse<GetSpendTagResponse>>(`${getApiBase()}/analytics/spend-tags`, {
        method: 'GET',
        query: request 
    });

    return response
}