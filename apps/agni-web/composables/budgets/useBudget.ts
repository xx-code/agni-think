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
                target: data.target,
                realTarget: data.realTarget,
                saveGoalIds: data.saveGoalIds,
                saveGoalTarget: data.saveGoalTarget,
                dueDate: new Date(data.dueDate),
                repeater: data.repeater
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
        target: res.target,
        realTarget: res.realTarget,
        saveGoalIds: res.saveGoalIds,
        saveGoalTarget: res.saveGoalTarget,
        dueDate: new Date(res.dueDate),
        repeater: res.repeater
    }
}