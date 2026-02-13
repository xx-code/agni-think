import type { Reactive } from "vue";
import type { GetSavingAnalysticRequest, GetSavingAnalysticResponse } from "~/types/api/analytics";
import type { SavingAnalysticType } from "~/types/ui/analytics";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useAnalyticSavings(request: Reactive<GetSavingAnalysticRequest>): UseApiFetchReturn<SavingAnalysticType>{
    const { data, error, refresh } = useFetch('/api/analytics/savings', {
        method: 'GET',
        query: request,
        transform: (data: GetSavingAnalysticResponse) => {
            return {
                savings: data.savings,
                investments: data.investments,
                savingRates: data.savingRates,
                investementRates: data.investmentRates
            } satisfies SavingAnalysticType
        }
    });

    return { data, error, refresh }
}

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