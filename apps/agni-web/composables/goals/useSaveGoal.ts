import type { GetSavingGoalResponse } from "~/types/api/saveGoal";
import type { SaveGoalType } from "~/types/ui/saveGoal";

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