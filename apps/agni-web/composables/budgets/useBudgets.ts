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
                    saveGoalIds: i.saveGoalIds,
                    currentBalance: i.currentBalance,
                    target: i.target,
                    realTarget: i.realTarget,
                    saveGoalTarget: i.saveGoalTarget,
                    dueDate: new Date(i.dueDate),
                    repeater: i.repeater
                })),
               totals: Number(data.totals) 
            } satisfies ListResponse<GetAllBudgetResponse>
        }
    });

    return { data, error, refresh }
}