import type { ListResponse } from "~/types/api";
import type { GetAllBudgetResponse } from "~/types/api/budget";
import type { BudgetType } from "~/types/ui/budget";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useBudgets(): UseApiFetchReturn<ListResponse<BudgetType>> {
    const { data, error, refresh } = useFetch('/api/budgets', {
        method: 'GET',
        transform: (data: ListResponse<GetAllBudgetResponse>) => {
            return {
                items: data.items.map(i => ({
                    id: i.id,
                    title: i.title,
                    period: i.period,
                    periodTime: i.periodTime,
                    currentBalance: i.currentBalance,
                    startDate: i.startDate,
                    updateDate: i.updateDate,
                    target: i.target,
                    endDate: i.endDate   
                })),
                totals: data.totals
            } satisfies ListResponse<GetAllBudgetResponse>
        }
    });

    return { data, error, refresh }
}