import type { GetAnnualOutlookResponse, GetBudgetingRuleRequest, GetBudgetingRuleResponse, GetSavingAnalysticRequest, GetSavingAnalysticResponse } from "~/types/api/analytics";
import type { SavingAnalysticType } from "~/types/ui/analytics";

export async function fetchAnnualOutlook(): Promise<GetAnnualOutlookResponse>{
    const response = await $fetch<GetAnnualOutlookResponse>('/api/analytics/annual-outlook', {
        method: 'GET'
    });

    return response 
}

export async function fetchBudgetingRule(request: GetBudgetingRuleRequest): Promise<GetBudgetingRuleResponse> {
    return await $fetch('/api/analytics/budgeting-rule', {
        method: 'GET',
        query: request
    })
}