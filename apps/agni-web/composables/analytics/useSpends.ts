import type { Reactive } from "vue";
import type { GetSpendAnalysticRequest, GetSpendAnalysticResponse } from "~/types/api/analytics";
import type { SpendAnalysticType } from "~/types/ui/analytics";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useAnalyticSpends(request: Reactive<GetSpendAnalysticRequest>): UseApiFetchReturn<SpendAnalysticType>{
    const { data, error, refresh } = useFetch('/api/analytics/spends', {
        method: 'GET',
        query: request,
        transform: (data: GetSpendAnalysticResponse) => {
            return {
                totalSpends: data.totalSpends,
                spendByCategories: data.spendByCategories
            } satisfies SpendAnalysticType
        }
    });

    return { data, error, refresh }
}

export async function fetchAnalyticSpends(request: GetSpendAnalysticRequest): Promise<SpendAnalysticType>{
    const response = await $fetch<GetSpendAnalysticResponse>('/api/analytics/spends', {
        method: 'GET',
        query: request 
    });

    return { 
        totalSpends: response.totalSpends,
        spendByCategories: response.spendByCategories
    };
}