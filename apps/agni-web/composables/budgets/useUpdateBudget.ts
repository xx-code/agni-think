import type { UpdateBudgetRequest } from "~/types/api/budget";

export default async function useUpdateBudget(budgetId: string, request: UpdateBudgetRequest): Promise<void> {
    await $fetch(`/api/budgets/${budgetId}`, {
        method: 'PUT',
        body: request
    })
}