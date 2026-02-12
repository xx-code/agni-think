import type { DeleteSavingGoalRequest } from "~/types/api/saveGoal";

export default async function useDeleteSaveGoal(saveGoalId: string, request: DeleteSavingGoalRequest): Promise<void> {
    await $fetch(`/api/saving-goals/${saveGoalId}`, {
        method: 'DELETE',
        body: request
    });
}