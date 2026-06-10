import type { CreatedRequest } from "~/types/api";
import type { CreateBudgetRequest } from "~/types/api/budget";
import type { UpdateBudgetRequest } from "~/types/api/budget";

export async function fetchBudget(budgetId: string): Promise<BudgetType> {
    const res = await $fetch<GetBudgetResponse>(`${getApiBase()}/budgets/${budgetId}`, {
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

import type { ListResponse, QueryFilterRequest } from "~/types/api";
import type { GetBudgetResponse } from "~/types/api/budget";
import type { BudgetType } from "~/types/ui/budget";

export async function fetchBudgets(query: QueryFilterRequest): Promise<ListResponse<BudgetType>> {
    const res = await $fetch<ListResponse<GetBudgetResponse>>(`${getApiBase()}/budgets`, {
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

export async function useCreateBudget(request: CreateBudgetRequest): Promise<CreatedRequest> {
    const created = await $fetch<CreatedRequest>(`${getApiBase()}/budgets`, {
        method: 'POST',
        body: request
    });

    return created
}   

export async function useDeleteBudget(budgetId: string): Promise<void> {
    await $fetch(`${getApiBase()}/budgets/${budgetId}`, {
        method: 'DELETE'
    });
}

export async function useUpdateBudget(budgetId: string, request: UpdateBudgetRequest): Promise<void> {
    await $fetch(`${getApiBase()}/budgets/${budgetId}`, {
        method: 'PUT',
        body: request
    })
}