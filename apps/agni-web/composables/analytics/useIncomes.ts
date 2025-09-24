import type { Reactive } from "vue";
import type { GetIncomeAnalysticRequest, GetIncomeAnalysticResponse } from "~/types/api/analytics";
import type { IncomeAnalysticType } from "~/types/ui/analytics";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useAnalyticIncomes(request: Reactive<GetIncomeAnalysticRequest>): UseApiFetchReturn<IncomeAnalysticType>{
    const { data, error, refresh } = useFetch('/api/analytics/incomes', {
        method: 'GET',
        query: request,
        transform: (data: GetIncomeAnalysticResponse) => {
            return {
                incomes: data.incomes,
                incomesByDescription: data.incomesByDescription
            } satisfies IncomeAnalysticType
        }
    });

    return { data, error, refresh }
}

export async function fetchAnalyticIncomes(request: GetIncomeAnalysticRequest): Promise<IncomeAnalysticType>{
    const response = await $fetch<GetIncomeAnalysticResponse>('/api/analytics/incomes', {
        method: 'GET',
        query: request 
    });

    return { 
        incomes: response.incomes,
        incomesByDescription: response.incomesByDescription
    };
}