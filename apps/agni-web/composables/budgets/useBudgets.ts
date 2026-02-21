import type { ListResponse, QueryFilterRequest } from "~/types/api";
import type { GetBudgetResponse } from "~/types/api/budget";
import type { BudgetType } from "~/types/ui/budget";

export async function fetchBudgets(query: QueryFilterRequest): Promise<ListResponse<BudgetType>> {
    const res = await $fetch<ListResponse<GetBudgetResponse>>('/api/budgets', {
        method: 'GET',
        query: query
    });

    return {
        items: res.items.map(i => ({
                id: i.id,
                title: i.title,
                saveGoalIds: i.savingGoalIds,
                currentBalance: i.currentBalance,
                target: i.target,
                realTarget: i.realTarget,
                saveGoalTarget: i.savingGoalTarget,
                dueDate: new Date(i.dueDate),
                repeater: i.repeater
            } satisfies BudgetType)) ,
        total: res.total
    } 
}