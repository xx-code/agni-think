import type { UpdateSaveGoalRequest } from "~/types/api/saveGoal";

export default async function useUpdateSaveGaol(saveGoalId: string, request: UpdateSaveGoalRequest): Promise<void> {
    await $fetch(`/api/save-goals/${saveGoalId}`, {
        method: 'PUT',
        body: request
    }) 
}