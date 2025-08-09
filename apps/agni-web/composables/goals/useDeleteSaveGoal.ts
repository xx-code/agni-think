import type { DeleteSaveGoalRequest } from "~/types/api/saveGoal";

export default async function useDeleteSaveGoal(saveGoalId: string, request: DeleteSaveGoalRequest): Promise<void> {
    await $fetch(`/api/save-goals/${saveGoalId}`, {
        method: 'DELETE',
        body: request
    });
}