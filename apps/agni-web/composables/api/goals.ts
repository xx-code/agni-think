import { goalResponseToGoal } from "~/mappers/goal";
import type { CreatedRequest, ListResponse } from "~/types/api";
import type { CreateGoalRequest, GoalQueryFilterRequest, GoalResponse, UpdateGoalRequest } from "~/types/api/goal";
import type { Goal } from "~/types/ui/goal";
import { ApiLinkBuilder } from "~/utils/ApiLinkBuilder";
import { API_ROUTES } from "~/shared/routes";

export async function createGoal(request: CreateGoalRequest): Promise<CreatedRequest> {
    return await ApiLinkBuilder
        .route<CreatedRequest>(API_ROUTES.GOALS.CREATE_GOAL)
        .body(request)
        .execute()
}

export async function updateGoal(id: string, request: UpdateGoalRequest): Promise<void> {
    await ApiLinkBuilder
        .route(API_ROUTES.GOALS.UPDATE_GOAL)
        .params({ id: id })
        .body(request)
        .execute()
}

export async function fetchGoal(id: string): Promise<Goal> {
    return await ApiLinkBuilder
        .route<GoalResponse>(API_ROUTES.GOALS.GET_GOAL)
        .params({ id: id })
        .mapper(goalResponseToGoal)
        .execute()
}

export async function fetchAllGoal(query: GoalQueryFilterRequest): Promise<ListResponse<Goal>> {
    const res = await ApiLinkBuilder
        .route<ListResponse<GoalResponse>>(API_ROUTES.GOALS.GET_GOALS)
        .query(query)
        .execute()

    return {
        items: res.items.map(i => goalResponseToGoal(i)),
        total: res.total
    }
}

export async function deleteGoal(id: string): Promise<void> {
    await ApiLinkBuilder
        .route(API_ROUTES.GOALS.DELETE_GOAL)
        .params({ id: id })
        .execute()
}
