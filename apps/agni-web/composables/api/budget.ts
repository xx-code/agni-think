import type { CreatedRequest } from "~/types/api";
import type { CreateBudgetRequest, GetBudgetResponse, UpdateBudgetRequest } from "~/types/api/budget";
import type { ListResponse, QueryFilterRequest } from "~/types/api";
import { budgetFilterToBudgetQueryRequest, budgetResponseToBudget, listBudgetsResponseToListBudgets } from "~/mappers/budget";
import type { BudgetFilter, BudgetType } from "~/types/ui/budget";
import { ApiLinkBuilder } from "~/utils/ApiLinkBuilder";
import { API_ROUTES } from "~/shared/routes";

export async function fetchBudget(budgetId: string): Promise<BudgetType> {
    return await ApiLinkBuilder
        .route<GetBudgetResponse>(API_ROUTES.BUDGETS.GET_BUDGET)
        .params({ id: budgetId })
        .mapper(budgetResponseToBudget)
        .execute()
}

export async function fetchBudgets(query: BudgetFilter): Promise<ListResponse<BudgetType>> {
    return await ApiLinkBuilder
        .route(API_ROUTES.BUDGETS.GET_BUDGETS)
        .query(budgetFilterToBudgetQueryRequest(query))
        .mapper(listBudgetsResponseToListBudgets)
        .execute()
}

export async function useCreateBudget(request: CreateBudgetRequest): Promise<CreatedRequest> {
    return await ApiLinkBuilder
        .route<CreatedRequest>(API_ROUTES.BUDGETS.CREATE_BUDGET)
        .body(request)
        .execute()
}

export async function useDeleteBudget(budgetId: string): Promise<void> {
    await ApiLinkBuilder
        .route(API_ROUTES.BUDGETS.DELETE_BUDGET)
        .params({ id: budgetId })
        .execute()
}

export async function useUpdateBudget(budgetId: string, request: UpdateBudgetRequest): Promise<void> {
    await ApiLinkBuilder
        .route(API_ROUTES.BUDGETS.UPDATE_BUDGET)
        .params({ id: budgetId })
        .body(request)
        .execute()
}
