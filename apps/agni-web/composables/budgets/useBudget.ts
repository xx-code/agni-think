import type { GetBudgetResponse } from "~/types/api/budget";
import type { BudgetType } from "~/types/ui/budget";

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
        saveGoalIds: res.savingGoalIds,
        saveGoalTarget: res.savingGoalTarget,
        dueDate: new Date(res.dueDate),
        repeater: res.repeater
    }
}