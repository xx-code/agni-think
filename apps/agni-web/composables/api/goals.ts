import { goalResponseToGoal } from "~/mappers/goal";
import type { CreatedRequest, ListResponse, QueryFilterRequest } from "~/types/api";
import type { CreateGoalRequest, GoalQueryFilterRequest, GoalResponse, UpdateGoalRequest } from "~/types/api/goal";
import type { Goal } from "~/types/ui/goal";

export async function createGoal(request: CreateGoalRequest): Promise<CreatedRequest> {
    return await $fetch<CreatedRequest>('/api/goals', {
        method: 'POST',
        body: request
    })
}

export async function updateGoal(id: string, request: UpdateGoalRequest): Promise<void> {
    await $fetch(`/api/goals/${id}`, {
        method: 'PUT',
        body: request
    })
}

export async function fetchGoal(id: string): Promise<Goal> {
    const res = await $fetch<GoalResponse>(`/api/goals/${id}`)
    return goalResponseToGoal(res) 
}

export async function fetchAllGoal(query: GoalQueryFilterRequest): Promise<ListResponse<Goal>> {
    const res = await $fetch<ListResponse<GoalResponse>>('/api/goals', {
        query
    })

    return {
        items: res.items.map(i => goalResponseToGoal(i)),
        total: res.total
    } 
}

export async function deleteGoal(id: string): Promise<void> {
    await $fetch(`/api/goals/${id}`, {
        method: 'DELETE'
    })
}