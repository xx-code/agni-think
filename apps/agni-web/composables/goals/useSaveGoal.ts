import type { GetSavingGoalResponse } from "~/types/api/saveGoal";
import type { SaveGoalType } from "~/types/ui/saveGoal";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useSaveGoal(saveGoalId: string): UseApiFetchReturn<SaveGoalType>{
    const { data, error, refresh } = useFetch(`/api/saving-goals/${saveGoalId}`, {
        method: 'GET',
        transform: (data: GetSavingGoalResponse) => {
            return {
                id: data.id,
                title: data.title,
                description: data.description,
                balance: data.balance,
                target: data.target,
                items: data.items,
                accountId: data.accountId,
                importance: data.importance,
                desirValue: data.desirValue,
                wishDueDate: data.wishDueDate ? new Date(data.wishDueDate) : undefined
            } satisfies SaveGoalType
        }
    });

    return { data, error, refresh };
}

export async function fetchSaveGoal(saveGoalId: string): Promise<SaveGoalType> {
    const res = await $fetch<GetSavingGoalResponse>(`/api/saving-goals/${saveGoalId}`, {
        method: 'GET'
    });

    return {
        id: res.id,
        title: res.title,
        description: res.description,
        balance: res.balance,
        target: res.target,
        accountId: res.accountId,
        importance: res.importance,
        desirValue: res.desirValue,
        wishDueDate: res.wishDueDate ? new Date(res.wishDueDate) : undefined,
        items: res.items
    };
}