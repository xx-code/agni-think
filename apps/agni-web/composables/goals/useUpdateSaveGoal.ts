import type { UpdateSavingGoalRequest } from "~/types/api/saveGoal";

export default async function useUpdateSaveGaol(saveGoalId: string, request: UpdateSavingGoalRequest): Promise<void> {
    await $fetch(`/api/saving-goals/${saveGoalId}`, {
        method: 'PUT',
        body: request
    }) 
}