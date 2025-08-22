import type { GetSaveGoalResponse } from "~/types/api/saveGoal";
import type { SaveGoalType } from "~/types/ui/saveGoal";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useSaveGoal(saveGoalId: string): UseApiFetchReturn<SaveGoalType>{
    const { data, error, refresh } = useFetch(`/api/save-goals/${saveGoalId}`, {
        method: 'GET',
        transform: (data: GetSaveGoalResponse) => {
            return {
                id: data.id,
                title: data.title,
                description: data.description,
                balance: data.balance,
                target: data.target,
                items: data.items,
                importance: data.importance,
                desirValue: data.desirValue,
                wishDueDate: data.wishDueDate ? new Date(data.wishDueDate) : undefined
            } satisfies SaveGoalType
        }
    });

    return { data, error, refresh };
}

export async function fetchSaveGoal(saveGoalId: string): Promise<SaveGoalType> {
    const res = await $fetch<GetSaveGoalResponse>(`/api/save-goals/${saveGoalId}`, {
        method: 'GET'
    });

    return {
        id: res.id,
        title: res.title,
        description: res.description,
        balance: res.balance,
        target: res.target,
        importance: res.importance,
        desirValue: res.desirValue,
        wishDueDate: res.wishDueDate ? new Date(res.wishDueDate) : undefined,
        items: res.items
    };
}