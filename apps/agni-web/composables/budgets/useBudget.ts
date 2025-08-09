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
                startDate: data.startDate,
                target: data.target,
                updateDate: data.updateDate,
                endDate: data.endDate
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
        startDate: res.startDate,
        target: res.target,
        updateDate: res.updateDate,
        endDate: res.endDate
    }
}