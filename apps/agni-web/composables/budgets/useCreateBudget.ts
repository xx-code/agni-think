import type { CreatedRequest } from "~/types/api";
import type { CreateBudgetRequest } from "~/types/api/budget";

export default async function useCreateBudget(request: CreateBudgetRequest): Promise<CreatedRequest> {
    const created = await $fetch<CreatedRequest>('/api/budgets', {
        method: 'POST',
        body: request
    });

    return created
}   