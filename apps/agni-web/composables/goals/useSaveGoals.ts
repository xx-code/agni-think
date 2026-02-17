import type { ListResponse } from "~/types/api";
import type { GetSavingGoalResponse, QueryFilterSavingGoalRequest } from "~/types/api/saveGoal";
import type { SaveGoalType } from "~/types/ui/saveGoal";

export async function fetchSavingGoals(query: QueryFilterSavingGoalRequest) : Promise<ListResponse<SaveGoalType>> {
    const res = await $fetch<ListResponse<GetSavingGoalResponse>>('/api/saving-goals', {
        method: 'GET',
        query: query
    })

    return {
        items: res.items.map(data => {
            return {
                id: data.id,
                title: data.title,
                description: data.description,
                target: data.target,
                balance: data.balance,
                desirValue: data.desirValue,
                importance: data.importance,
                items: data.items.map(item => ({
                   title: item.title, 
                   url: item.url, 
                   price: item.price   
                }))
            } satisfies SaveGoalType
        }),
        total: res.total
    }
}