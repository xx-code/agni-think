import type { GetSavingAnalysticRequest, GetSavingAnalysticResponse } from "~/types/api/analytics";
import type { SavingAnalysticType } from "~/types/ui/analytics";

export async function fetchAnalyticSavings(request: GetSavingAnalysticRequest): Promise<SavingAnalysticType>{
    const response = await $fetch<GetSavingAnalysticResponse>('/api/analytics/savings', {
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