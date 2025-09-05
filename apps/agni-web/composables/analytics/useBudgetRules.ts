import type { Reactive } from "vue";
import type { GetAnalyseBudgeRuleRequest, GetAnalyseBudgeRuleResponse } from "~/types/api/analytics";
import type { GetAnalyseBudgeRuleType } from "~/types/ui/analytics";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useAnalyseBudgetRules(query: Reactive<GetAnalyseBudgeRuleRequest>): UseApiFetchReturn<GetAnalyseBudgeRuleType[]>{
    const { data, error, refresh } = useFetch('/api/analytics/budgetRules', {
        method: 'GET',
        query: query,
        transform: (data: GetAnalyseBudgeRuleResponse[]) => {
            return data.map(i => ({ value: i.value, transactionType: i.transactionType } satisfies GetAnalyseBudgeRuleType))
        }
    });
    
    return  { data, error, refresh }
} 

export async function fetchAnalyseBudgetRules(query: GetAnalyseBudgeRuleRequest): Promise<GetAnalyseBudgeRuleType[]> {
    const res = await $fetch<GetAnalyseBudgeRuleResponse[]>('/api/analytics/budgetRules', {
        method: "GET",
        query: query
    })

    return res.map(i => ({value: i.value, transactionType: i.transactionType } satisfies GetAnalyseBudgeRuleType)  )
}