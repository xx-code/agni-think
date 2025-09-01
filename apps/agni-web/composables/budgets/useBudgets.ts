import type { Reactive } from "vue";
import type { ListResponse, QueryFilterRequest } from "~/types/api";
import type { GetAllBudgetResponse } from "~/types/api/budget";
import type { BudgetType } from "~/types/ui/budget";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useBudgets(query: Reactive<QueryFilterRequest>): UseApiFetchReturn<ListResponse<BudgetType>> {
    const { data, error, refresh } = useFetch('/api/budgets', {
        method: 'GET',
        query: query,
        transform: (data: ListResponse<GetAllBudgetResponse>) => {
            return {
                items: data.items.map(i => ({
                    id: i.id,
                    title: i.title,
                    period: i.period,
                    periodTime: i.periodTime,
                    currentBalance: i.currentBalance,
                    startDate: new Date(i.startDate),
                    updateDate: new Date(i.updateDate),
                    target: i.target,
                    endDate: i.endDate   
                })),
                totals: data.totals
            } satisfies ListResponse<GetAllBudgetResponse>
        }
    });

    return { data, error, refresh }
}