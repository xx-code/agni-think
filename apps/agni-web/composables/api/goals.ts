import type { CreatedRequest, ListResponse, QueryFilterRequest } from "~/types/api";
import type { CreateGoalRequest, GoalResponse, UpdateGoalRequest } from "~/types/api/goal";

export async function createGoal(request: CreateGoalRequest): Promise<CreatedRequest> {
    return await $fetch<CreatedRequest>('/api/goals', {
        body: request
    })
}

export async function updateGoal(id: string, request: UpdateGoalRequest): Promise<void> {
    await $fetch(`/api/goals/${id}`, {
        method: 'PUT',
        body: request
    })
}

export async function fetchGoal(id: string): Promise<GoalResponse> {
    return await $fetch<GoalResponse>(`/api/goals/${id}`)
}

export async function fetchAllGoal(query: QueryFilterRequest): Promise<ListResponse<GoalResponse>> {
    return await $fetch<ListResponse<GoalResponse>>('/api/goals', {
        query
    })
}

export async function deleteGoal(id: string): Promise<void> {
    await $fetch(`/api/goals/${id}`, {
        method: 'DELETE'
    })
}