import type { CreatedRequest } from "~/types/api";
import type { CreateBudgetRequest } from "~/types/api/budget";
import type { UpdateBudgetRequest } from "~/types/api/budget";
import type { ListResponse, QueryFilterRequest } from "~/types/api";
import type { GetBudgetResponse } from "~/types/api/budget";
import type { BudgetFilter, BudgetType } from "~/types/ui/budget";
import { budgetFilterToBudgetQueryRequest } from "~/mappers/budget";

export async function fetchBudget(budgetId: string): Promise<BudgetType> {
    const res = await $fetch<GetBudgetResponse>(`api/budgets/${budgetId}`, {
        method: "GET"
    });

    return {
        id: res.id,
        title: res.title,
        currentBalance: res.currentBalance,
        target: res.target,
        dueDate: new Date(res.dueDate),
        repeater: res.repeater
    }
}

export async function fetchBudgets(query: BudgetFilter): Promise<ListResponse<BudgetType>> {
    const res = await $fetch<ListResponse<GetBudgetResponse>>(`api/budgets`, {
        method: 'GET',
        query: budgetFilterToBudgetQueryRequest(query)
    });

    return {
        items: res.items.map(i => ({
                id: i.id,
                title: i.title,
                currentBalance: i.currentBalance,
                target: i.target,
                dueDate: new Date(i.dueDate),
                repeater: i.repeater
            } satisfies BudgetType)) ,
        total: res.total
    } 
}

export async function useCreateBudget(request: CreateBudgetRequest): Promise<CreatedRequest> {
    const created = await $fetch<CreatedRequest>(`api/budgets`, {
        method: 'POST',
        body: request
    });

    return created
}   

export async function useDeleteBudget(budgetId: string): Promise<void> {
    await $fetch(`api/budgets/${budgetId}`, {
        method: 'DELETE'
    });
}

export async function useUpdateBudget(budgetId: string, request: UpdateBudgetRequest): Promise<void> {
    await $fetch(`api/budgets/${budgetId}`, {
        method: 'PUT',
        body: request
    })
}