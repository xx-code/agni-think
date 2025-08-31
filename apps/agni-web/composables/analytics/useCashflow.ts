import type { Reactive } from "vue";
import type { CashflowRequest } from "~/types/api/agent";
import type { GetCashflowAnalyseResponse } from "~/types/api/analytics";
import type { GetCashflowAnalyseType } from "~/types/ui/analytics";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useCashflow(query: Reactive<CashflowRequest>): UseApiFetchReturn<GetCashflowAnalyseType> { 
    const { data, error, refresh } = useFetch('/api/analytics/cashflow', {
        method: "GET",
        query: query,
        transform: (data: GetCashflowAnalyseResponse) => {
            return {
                incomes: data.gainsFlow,
                spends: data.spendFlow 
            } satisfies GetCashflowAnalyseType
        }
    }) 

    return { data, error, refresh } 
} 

export async function fetchCashflow(query: CashflowRequest): Promise<GetCashflowAnalyseType> {
    const res = await $fetch<GetCashflowAnalyseResponse>('/api/analytics/cashflow', {
        method: "GET",
        query: query
    }) 

    return {
        incomes: res.gainsFlow,
        spends: res.spendFlow
    }
}