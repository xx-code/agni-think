import type { GetBudgetResponse } from "~/types/api/budget";
import type { BudgetType } from "~/types/ui/budget";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useBudget(budgetId: string): UseApiFetchReturn<BudgetType> {
    const { data, error, refresh } = useFetch(`/api/budgets/${budgetId}`, {
        method: 'GET',
        transform: (data: GetBudgetResponse) => {
            return {
                id: data.id,
                title: data.title,
                currentBalance: data.currentBalance,
                period: data.period,
                periodTime: data.periodTime,
                startDate: new Date(data.startDate),
                target: data.target,
                updateDate: new Date(data.updateDate),
                endDate: data.endDate ? new Date(data.endDate): undefined
            } satisfies BudgetType
        }
    });

    return { data, error, refresh };
}

export async function fetchBudget(budgetId: string): Promise<BudgetType> {
    const res = await $fetch<GetBudgetResponse>(`/api/budgets/${budgetId}`, {
        method: "GET"
    });

    return {
        id: res.id,
        title: res.title,
        currentBalance: res.currentBalance,
        period: res.period,
        periodTime: res.periodTime,
        startDate: new Date(res.startDate),
        target: res.target,
        updateDate: new Date(res.updateDate),
        endDate: res.endDate ? new Date(res.endDate) : undefined 
    }
}